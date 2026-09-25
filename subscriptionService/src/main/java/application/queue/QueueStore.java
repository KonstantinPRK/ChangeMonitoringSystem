package application.queue;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Repository
public class QueueStore {
    private final JdbcClient database;


    public QueueStore(JdbcClient database) {
        this.database = database;
    }


    public QueueReceipt publish(QueueKind kind, UUID messageId, String streamKey, String payload) {
        database.sql("""
                INSERT INTO service_queue (message_id, kind, stream_key, payload)
                VALUES (:messageId, :kind, :streamKey, :payload)
                ON CONFLICT (kind, message_id) DO NOTHING
                """).param("messageId", messageId).param("kind", kind.name())
                .param("streamKey", streamKey).param("payload", payload).update();
        String stored = database.sql("SELECT payload FROM service_queue WHERE kind = :kind AND message_id = :id")
                .param("kind", kind.name()).param("id", messageId).query(String.class).single();
        if (!stored.equals(payload)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Message ID already has another payload");
        return status(kind, messageId);
    }


    public QueueReceipt status(QueueKind kind, UUID messageId) {
        return database.sql("""
                SELECT message_id, status, attempts, last_error FROM service_queue
                WHERE kind = :kind AND message_id = :id
                """).param("kind", kind.name()).param("id", messageId)
                .query((row, index) -> new QueueReceipt(
                        row.getObject("message_id", UUID.class),
                        row.getString("status"),
                        row.getInt("attempts"),
                        row.getString("last_error")
                )).optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }


    public Optional<QueueReceipt> findExisting(QueueKind kind, UUID messageId, String payload) {
        Optional<String> stored = database.sql("SELECT payload FROM service_queue WHERE kind = :kind AND message_id = :id")
                .param("kind", kind.name()).param("id", messageId).query(String.class).optional();
        if (stored.isEmpty()) return Optional.empty();
        if (!stored.get().equals(payload)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Message ID already has another payload");
        return Optional.of(status(kind, messageId));
    }


    public Optional<QueueMessage> claim(QueueKind kind, int leaseSeconds) {
        UUID token = UUID.randomUUID();
        return database.sql("""
                WITH candidate AS (
                    SELECT queue.id FROM service_queue queue
                    WHERE queue.kind = :kind
                      AND ((queue.status = 'PENDING' AND queue.available_at <= CURRENT_TIMESTAMP)
                        OR (queue.status = 'PROCESSING' AND queue.locked_until < CURRENT_TIMESTAMP))
                      AND NOT EXISTS (
                          SELECT 1 FROM service_queue earlier
                          WHERE earlier.kind = queue.kind AND earlier.stream_key = queue.stream_key
                            AND earlier.id < queue.id AND earlier.status IN ('PENDING', 'PROCESSING')
                      )
                    ORDER BY queue.id FOR UPDATE SKIP LOCKED LIMIT 1
                )
                UPDATE service_queue queue
                SET status = 'PROCESSING', claim_token = :token,
                    locked_until = CURRENT_TIMESTAMP + :lease * INTERVAL '1 second', attempts = attempts + 1
                FROM candidate WHERE queue.id = candidate.id
                RETURNING queue.id, message_id, kind, payload, attempts, claim_token
                """).param("kind", kind.name()).param("token", token).param("lease", leaseSeconds)
                .query((row, index) -> new QueueMessage(
                        row.getLong("id"),
                        row.getObject("message_id", UUID.class),
                        QueueKind.valueOf(row.getString("kind")),
                        row.getString("payload"),
                        row.getInt("attempts"),
                        row.getObject("claim_token", UUID.class)
                )).optional();
    }


    public boolean lockClaim(QueueMessage message) {
        return database.sql("""
                SELECT id FROM service_queue
                WHERE id = :id AND claim_token = :token AND status = 'PROCESSING'
                FOR UPDATE
                """).param("id", message.id()).param("token", message.claimToken()).query(Long.class).optional().isPresent();
    }


    public void complete(QueueMessage message) {
        database.sql("""
                UPDATE service_queue SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP,
                    locked_until = NULL, claim_token = NULL, last_error = NULL
                WHERE id = :id AND claim_token = :token AND status = 'PROCESSING'
                """).param("id", message.id()).param("token", message.claimToken()).update();
    }


    public void fail(QueueMessage message, boolean permanent, String error) {
        long delay = Math.min(60L, 1L << Math.min(message.attempts(), 6));
        database.sql("""
                UPDATE service_queue SET status = :status, last_error = :error,
                    available_at = CURRENT_TIMESTAMP + :delay * INTERVAL '1 second',
                    locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :token AND status = 'PROCESSING'
                """).param("status", permanent ? "FAILED" : "PENDING").param("error", error)
                .param("delay", delay).param("id", message.id()).param("token", message.claimToken()).update();
    }
}
