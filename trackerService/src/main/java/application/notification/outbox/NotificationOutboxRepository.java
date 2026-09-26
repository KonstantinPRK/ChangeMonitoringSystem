package application.notification.outbox;

import application.config.WorkerProperties;
import application.notification.model.NotificationEvent;
import application.notification.model.StoredNotification;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code NotificationOutboxRepository}.
 */
@Repository
public class NotificationOutboxRepository {
    private final JdbcClient jdbcClient;
    private final WorkerProperties workerProperties;
    private final Clock clock;


    public NotificationOutboxRepository(
            JdbcClient jdbcClient,
            WorkerProperties workerProperties,
            Clock clock
    ) {
        this.jdbcClient = jdbcClient;
        this.workerProperties = workerProperties;
        this.clock = clock;
    }


    public void save(long trackedResourceId, NotificationEvent event) {
        Instant now = clock.instant();

        jdbcClient.sql("""
                INSERT INTO notification_outbox (
                    event_id, tracked_resource_id, payload_json, status, available_at, created_at
                ) VALUES (
                    :eventId, :trackedResourceId, CAST(:payloadJson AS jsonb),
                    'PENDING', :availableAt, :createdAt
                )
                ON CONFLICT (event_id) DO NOTHING
                """)
                .param("eventId", event.eventId())
                .param("trackedResourceId", trackedResourceId)
                .param("payloadJson", event.payloadJson())
                .param("availableAt", from(now))
                .param("createdAt", from(now))
                .update();
    }


    public Optional<StoredNotification> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();

        return jdbcClient.sql("""
                UPDATE notification_outbox current
                SET status = 'PROCESSING', attempts = attempts + 1,
                    locked_until = :lockedUntil, claim_token = :claimToken
                WHERE current.id = (
                    SELECT candidate.id FROM notification_outbox candidate
                    WHERE ((candidate.status IN ('PENDING', 'RETRY') AND candidate.available_at <= :now)
                        OR (candidate.status = 'PROCESSING' AND candidate.locked_until <= :now))
                      AND NOT EXISTS (
                          SELECT 1 FROM notification_outbox earlier
                          WHERE earlier.tracked_resource_id = candidate.tracked_resource_id
                            AND earlier.id < candidate.id
                            AND earlier.status IN ('PENDING', 'PROCESSING', 'RETRY')
                      )
                    ORDER BY candidate.id
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING id, event_id, tracked_resource_id, payload_json::text, attempts, claim_token
                """)
                .param("lockedUntil", from(now.plus(workerProperties.claimLease())))
                .param("claimToken", claimToken)
                .param("now", from(now))
                .query(this::mapNotification)
                .optional();
    }


    public void complete(StoredNotification notification) {
        jdbcClient.sql("""
                UPDATE notification_outbox
                SET status = 'COMPLETED', completed_at = :completedAt,
                    locked_until = NULL, claim_token = NULL
                WHERE id = :notificationId AND claim_token = :claimToken
                """)
                .param("completedAt", from(clock.instant()))
                .param("notificationId", notification.id())
                .param("claimToken", notification.claimToken())
                .update();
    }


    public void retry(StoredNotification notification, Duration delay, String error) {
        jdbcClient.sql("""
                UPDATE notification_outbox
                SET status = 'RETRY', available_at = :availableAt, last_error = :error,
                    locked_until = NULL, claim_token = NULL
                WHERE id = :notificationId AND claim_token = :claimToken
                """)
                .param("availableAt", from(clock.instant().plus(delay)))
                .param("error", error)
                .param("notificationId", notification.id())
                .param("claimToken", notification.claimToken())
                .update();
    }


    public void fail(StoredNotification notification, String error) {
        jdbcClient.sql("""
                UPDATE notification_outbox
                SET status = 'FAILED', last_error = :error,
                    locked_until = NULL, claim_token = NULL
                WHERE id = :notificationId AND claim_token = :claimToken
                """)
                .param("error", error)
                .param("notificationId", notification.id())
                .param("claimToken", notification.claimToken())
                .update();
    }


    private StoredNotification mapNotification(ResultSet resultSet, int rowNumber) throws SQLException {
        return new StoredNotification(
                resultSet.getLong("id"),
                resultSet.getObject("event_id", UUID.class),
                resultSet.getLong("tracked_resource_id"),
                resultSet.getString("payload_json"),
                resultSet.getInt("attempts"),
                resultSet.getObject("claim_token", UUID.class)
        );
    }
}
