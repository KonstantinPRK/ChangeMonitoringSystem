package application.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

@Repository
public class OutgoingMessageRepository {
    private static final Duration CLAIM_LEASE = Duration.ofSeconds(60);
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public OutgoingMessageRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public void save(String deduplicationKey, UUID userId, String chatId, String text) {
        jdbcTemplate.update(
                """
                INSERT INTO outgoing_messages (
                    id, deduplication_key, user_id, chat_id, text,
                    status, available_at, created_at
                ) VALUES (?, ?, ?, ?, ?, 'PENDING', ?, ?)
                ON CONFLICT (deduplication_key) DO NOTHING
                """,
                UUID.randomUUID(),
                deduplicationKey,
                userId,
                chatId,
                text,
                from(clock.instant()),
                from(clock.instant())
        );
    }


    public Optional<StoredOutgoingMessage> claimNext(String botId) {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();
        List<StoredOutgoingMessage> messages = jdbcTemplate.query(
                """
                UPDATE outgoing_messages AS outgoing
                SET status = 'PROCESSING', attempts = attempts + 1,
                    locked_until = ?, claim_token = ?
                WHERE outgoing.id = (
                    SELECT candidate.id
                    FROM outgoing_messages AS candidate
                    JOIN bot_users AS users ON users.id = candidate.user_id
                    WHERE users.bot_id = ?
                      AND (
                          (candidate.status IN ('PENDING', 'RETRY') AND candidate.available_at <= ?)
                          OR (candidate.status = 'PROCESSING' AND candidate.locked_until <= ?)
                      )
                    ORDER BY candidate.created_at
                    FOR UPDATE OF candidate SKIP LOCKED
                    LIMIT 1
                )
                RETURNING outgoing.id, outgoing.chat_id, outgoing.text,
                          outgoing.attempts, outgoing.claim_token
                """,
                (resultSet, rowNumber) -> mapMessage(resultSet, rowNumber, botId),
                from(now.plus(CLAIM_LEASE)),
                claimToken,
                botId,
                from(now),
                from(now)
        );
        return messages.stream().findFirst();
    }


    public void complete(StoredOutgoingMessage message, String messengerMessageId) {
        jdbcTemplate.update(
                """
                UPDATE outgoing_messages
                SET status = 'COMPLETED', messenger_message_id = ?,
                    locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                messengerMessageId,
                message.id(),
                message.claimToken()
        );
    }


    public void retry(StoredOutgoingMessage message, Duration delay, String error) {
        jdbcTemplate.update(
                """
                UPDATE outgoing_messages
                SET status = 'RETRY', available_at = ?, last_error = ?,
                    locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                from(clock.instant().plus(delay)),
                error,
                message.id(),
                message.claimToken()
        );
    }


    public void fail(StoredOutgoingMessage message, String error) {
        jdbcTemplate.update(
                """
                UPDATE outgoing_messages
                SET status = 'FAILED', last_error = ?, locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                error,
                message.id(),
                message.claimToken()
        );
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
