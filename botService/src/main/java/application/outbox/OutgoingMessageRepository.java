package application.outbox;

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
 * Отвечает за сохранение и чтение данных {@code OutgoingMessageRepository}.
 */
@Repository
public class OutgoingMessageRepository {
    private static final Duration CLAIM_LEASE = Duration.ofSeconds(60);
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public OutgoingMessageRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public void save(String deduplicationKey, UUID userId, String chatId, String text) {
        Instant now = clock.instant();

        jdbcClient.sql("""
                INSERT INTO outgoing_messages (
                    id, deduplication_key, user_id, chat_id, text,
                    status, available_at, created_at
                ) VALUES (:id, :deduplicationKey, :userId, :chatId, :text, 'PENDING', :availableAt, :createdAt)
                ON CONFLICT (deduplication_key) DO NOTHING
                """)
                .param("id", UUID.randomUUID())
                .param("deduplicationKey", deduplicationKey)
                .param("userId", userId)
                .param("chatId", chatId)
                .param("text", text)
                .param("availableAt", from(now))
                .param("createdAt", from(now))
                .update();
    }


    public Optional<StoredOutgoingMessage> claimNext(String botId) {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();

        return jdbcClient.sql("""
                UPDATE outgoing_messages AS outgoing
                SET status = 'PROCESSING', attempts = attempts + 1,
                    locked_until = :lockedUntil, claim_token = :claimToken
                WHERE outgoing.id = (
                    SELECT candidate.id
                    FROM outgoing_messages AS candidate
                    JOIN bot_users AS users ON users.id = candidate.user_id
                    WHERE users.bot_id = :botId
                      AND (
                          (candidate.status IN ('PENDING', 'RETRY') AND candidate.available_at <= :now)
                          OR (candidate.status = 'PROCESSING' AND candidate.locked_until <= :now)
                      )
                    ORDER BY candidate.created_at
                    FOR UPDATE OF candidate SKIP LOCKED
                    LIMIT 1
                )
                RETURNING outgoing.id, outgoing.chat_id, outgoing.text,
                          outgoing.attempts, outgoing.claim_token
                """)
                .param("lockedUntil", from(now.plus(CLAIM_LEASE)))
                .param("claimToken", claimToken)
                .param("botId", botId)
                .param("now", from(now))
                .query((resultSet, rowNumber) -> mapMessage(resultSet, rowNumber, botId))
                .optional();
    }


    public void complete(StoredOutgoingMessage message, String messengerMessageId) {
        jdbcClient.sql("""
                UPDATE outgoing_messages
                SET status = 'COMPLETED', messenger_message_id = :messengerMessageId,
                    locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("messengerMessageId", messengerMessageId)
                .param("id", message.id())
                .param("claimToken", message.claimToken())
                .update();
    }


    public void retry(StoredOutgoingMessage message, Duration delay, String error) {
        jdbcClient.sql("""
                UPDATE outgoing_messages
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


    public void fail(StoredOutgoingMessage message, String error) {
        jdbcClient.sql("""
                UPDATE outgoing_messages
                SET status = 'FAILED', last_error = :error, locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("error", error)
                .param("id", message.id())
                .param("claimToken", message.claimToken())
                .update();
    }


    private StoredOutgoingMessage mapMessage(
            ResultSet resultSet,
            int rowNumber,
            String botId
    ) throws SQLException {
        return new StoredOutgoingMessage(
                resultSet.getObject("id", UUID.class),
                botId,
                resultSet.getString("chat_id"),
                resultSet.getString("text"),
                resultSet.getInt("attempts"),
                resultSet.getObject("claim_token", UUID.class)
        );
    }
}
