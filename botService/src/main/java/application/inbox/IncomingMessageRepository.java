package application.inbox;

import application.messenger.IncomingMessage;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code IncomingMessageRepository}.
 */
@Repository
public class IncomingMessageRepository {
    private static final Duration CLAIM_LEASE = Duration.ofSeconds(60);
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public IncomingMessageRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public void save(String botId, IncomingMessage message) {
        jdbcClient.sql("""
                INSERT INTO incoming_messages (
                    id, source, external_id, external_user_id, chat_id, text,
                    received_at, status, available_at
                ) VALUES (
                    :id, :source, :externalId, :externalUserId, :chatId, :text,
                    :receivedAt, 'PENDING', :availableAt
                )
                ON CONFLICT (source, external_id) DO NOTHING
                """)
                .param("id", UUID.randomUUID())
                .param("source", botId)
                .param("externalId", message.externalId())
                .param("externalUserId", message.externalUserId())
                .param("chatId", message.chatId())
                .param("text", message.text())
                .param("receivedAt", from(message.receivedAt()))
                .param("availableAt", from(clock.instant()))
                .update();
    }


    public void saveCheckpoint(String botId, long nextOffset) {
        jdbcClient.sql("""
                INSERT INTO messenger_checkpoints (source, next_offset) VALUES (:source, :nextOffset)
                ON CONFLICT (source) DO UPDATE SET next_offset = GREATEST(
                    messenger_checkpoints.next_offset,
                    EXCLUDED.next_offset
                )
                """)
                .param("source", botId)
                .param("nextOffset", nextOffset)
                .update();
    }


    public long checkpoint(String botId) {
        return jdbcClient.sql("SELECT next_offset FROM messenger_checkpoints WHERE source = :source")
                .param("source", botId)
                .query(Long.class)
                .optional()
                .orElse(0L);
    }


    public Optional<StoredIncomingMessage> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();

        return jdbcClient.sql("""
                UPDATE incoming_messages
                SET status = 'PROCESSING', attempts = attempts + 1,
                    locked_until = :lockedUntil, claim_token = :claimToken
                WHERE id = (
                    SELECT id FROM incoming_messages
                    WHERE (status IN ('PENDING', 'RETRY') AND available_at <= :now)
                       OR (status = 'PROCESSING' AND locked_until <= :now)
                    ORDER BY received_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING id, source, external_id, external_user_id, chat_id,
                          text, received_at, attempts, claim_token
                """)
                .param("lockedUntil", from(now.plus(CLAIM_LEASE)))
                .param("claimToken", claimToken)
                .param("now", from(now))
                .query(this::mapMessage)
                .optional();
    }


    public void complete(StoredIncomingMessage message) {
        jdbcClient.sql("""
                UPDATE incoming_messages
                SET status = 'COMPLETED', locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("id", message.id())
                .param("claimToken", message.claimToken())
                .update();
    }


    public void retry(StoredIncomingMessage message, Duration delay, String error) {
        jdbcClient.sql("""
                UPDATE incoming_messages
                SET status = 'RETRY', available_at = :availableAt, last_error = :error,
                    locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("availableAt", from(clock.instant().plus(delay)))
                .param("error", error)
                .param("id", message.id())
                .param("claimToken", message.claimToken())
                .update();
    }


    public void fail(StoredIncomingMessage message, String error) {
        jdbcClient.sql("""
                UPDATE incoming_messages
                SET status = 'FAILED', last_error = :error, locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("error", error)
                .param("id", message.id())
                .param("claimToken", message.claimToken())
                .update();
    }


    private StoredIncomingMessage mapMessage(ResultSet resultSet, int rowNumber) throws SQLException {
        return new StoredIncomingMessage(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("source"),
                resultSet.getLong("external_id"),
                resultSet.getString("external_user_id"),
                resultSet.getString("chat_id"),
                resultSet.getString("text"),
                resultSet.getTimestamp("received_at").toInstant(),
                resultSet.getInt("attempts"),
                resultSet.getObject("claim_token", UUID.class)
        );
    }
}
