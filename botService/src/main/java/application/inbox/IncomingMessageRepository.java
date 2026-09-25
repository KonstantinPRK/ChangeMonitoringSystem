package application.inbox;

import application.messenger.IncomingMessage;

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
public class IncomingMessageRepository {
    private static final Duration CLAIM_LEASE = Duration.ofSeconds(60);
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public IncomingMessageRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public void save(String botId, IncomingMessage message) {
        jdbcTemplate.update(
                """
                INSERT INTO incoming_messages (
                    id, source, external_id, external_user_id, chat_id, text,
                    received_at, status, available_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)
                ON CONFLICT (source, external_id) DO NOTHING
                """,
                UUID.randomUUID(),
                botId,
                message.externalId(),
                message.externalUserId(),
                message.chatId(),
                message.text(),
                from(message.receivedAt()),
                from(clock.instant())
        );
    }


    public void saveCheckpoint(String botId, long nextOffset) {
        jdbcTemplate.update(
                """
                INSERT INTO messenger_checkpoints (source, next_offset) VALUES (?, ?)
                ON CONFLICT (source) DO UPDATE SET next_offset = GREATEST(
                    messenger_checkpoints.next_offset,
                    EXCLUDED.next_offset
                )
                """,
                botId,
                nextOffset
        );
    }


    public long checkpoint(String botId) {
        List<Long> offsets = jdbcTemplate.query(
                "SELECT next_offset FROM messenger_checkpoints WHERE source = ?",
                (resultSet, rowNumber) -> resultSet.getLong("next_offset"),
                botId
        );
        return offsets.stream().findFirst().orElse(0L);
    }


    public Optional<StoredIncomingMessage> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();
        List<StoredIncomingMessage> messages = jdbcTemplate.query(
                """
                UPDATE incoming_messages
                SET status = 'PROCESSING', attempts = attempts + 1,
                    locked_until = ?, claim_token = ?
                WHERE id = (
                    SELECT id FROM incoming_messages
                    WHERE (status IN ('PENDING', 'RETRY') AND available_at <= ?)
                       OR (status = 'PROCESSING' AND locked_until <= ?)
                    ORDER BY received_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING id, source, external_id, external_user_id, chat_id,
                          text, received_at, attempts, claim_token
                """,
                this::mapMessage,
                from(now.plus(CLAIM_LEASE)),
                claimToken,
                from(now),
                from(now)
        );
        return messages.stream().findFirst();
    }


    public void complete(StoredIncomingMessage message) {
        jdbcTemplate.update(
                """
                UPDATE incoming_messages
                SET status = 'COMPLETED', locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                message.id(),
                message.claimToken()
        );
    }


    public void retry(StoredIncomingMessage message, Duration delay, String error) {
        jdbcTemplate.update(
                """
                UPDATE incoming_messages
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


    public void fail(StoredIncomingMessage message, String error) {
        jdbcTemplate.update(
                """
                UPDATE incoming_messages
                SET status = 'FAILED', last_error = ?, locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                error,
                message.id(),
                message.claimToken()
        );
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
