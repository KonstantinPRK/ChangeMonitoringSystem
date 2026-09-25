package application.subscription.operation;

import application.persistence.JsonValues;
import application.subscription.model.Link;
import application.user.BotUser;
import application.user.UserKey;
import application.user.UserStatus;

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
public class SubscriptionOperationRepository {
    private static final Duration CLAIM_LEASE = Duration.ofSeconds(60);
    private final JdbcTemplate jdbcTemplate;
    private final JsonValues jsonValues;
    private final Clock clock;


    public SubscriptionOperationRepository(JdbcTemplate jdbcTemplate, JsonValues jsonValues, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonValues = jsonValues;
        this.clock = clock;
    }


    public void save(UUID operationId, BotUser user, SubscriptionOperationDraft draft) {
        Link link = draft.link();
        jdbcTemplate.update(
                """
                INSERT INTO subscription_operations (
                    id, operation_type, user_id, link_domain, link_address,
                    tags_json, filters_json, status, available_at, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                operationId,
                draft.type().name(),
                user.id(),
                link == null ? null : link.domain(),
                link == null ? null : link.address(),
                jsonValues.writeStrings(draft.tags()),
                jsonValues.writeStrings(draft.filters()),
                from(clock.instant()),
                from(clock.instant())
        );
    }


    public Optional<StoredSubscriptionOperation> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();
        List<StoredSubscriptionOperation> operations = jdbcTemplate.query(
                """
                WITH claimed AS (
                    UPDATE subscription_operations
                    SET status = 'PROCESSING', attempts = attempts + 1,
                        locked_until = ?, claim_token = ?
                    WHERE id = (
                        SELECT id FROM subscription_operations
                        WHERE (status IN ('PENDING', 'RETRY') AND available_at <= ?)
                           OR (status = 'PROCESSING' AND locked_until <= ?)
                        ORDER BY created_at
                        FOR UPDATE SKIP LOCKED
                        LIMIT 1
                    )
                    RETURNING *
                )
                SELECT claimed.*, users.bot_id, users.messenger, users.external_user_id,
                       users.chat_id, users.status AS user_status, users.created_at AS user_created_at
                FROM claimed
                JOIN bot_users users ON users.id = claimed.user_id
                """,
                this::mapOperation,
                from(now.plus(CLAIM_LEASE)),
                claimToken,
                from(now),
                from(now)
        );
        return operations.stream().findFirst();
    }


    public void complete(StoredSubscriptionOperation operation) {
        jdbcTemplate.update(
                """
                UPDATE subscription_operations
                SET status = 'COMPLETED', locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                operation.id(),
                operation.claimToken()
        );
    }


    public void retry(StoredSubscriptionOperation operation, Duration delay, String error) {
        jdbcTemplate.update(
                """
                UPDATE subscription_operations
                SET status = 'RETRY', available_at = ?, last_error = ?,
                    locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                from(clock.instant().plus(delay)),
                error,
                operation.id(),
                operation.claimToken()
        );
    }


    public void fail(StoredSubscriptionOperation operation, String error) {
        jdbcTemplate.update(
                """
                UPDATE subscription_operations
                SET status = 'FAILED', last_error = ?, locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                error,
                operation.id(),
                operation.claimToken()
        );
    }


    private StoredSubscriptionOperation mapOperation(ResultSet resultSet, int rowNumber) throws SQLException {
        BotUser user = mapUser(resultSet);
        String linkAddress = resultSet.getString("link_address");
        Link link = linkAddress == null ? null : new Link(
                resultSet.getString("link_domain"),
                linkAddress
        );
        return new StoredSubscriptionOperation(
                resultSet.getObject("id", UUID.class),
                SubscriptionOperationType.valueOf(resultSet.getString("operation_type")),
                user,
                link,
                jsonValues.readStrings(resultSet.getString("tags_json")),
                jsonValues.readStrings(resultSet.getString("filters_json")),
                resultSet.getInt("attempts"),
                resultSet.getObject("claim_token", UUID.class)
        );
    }


    private BotUser mapUser(ResultSet resultSet) throws SQLException {
        UserKey key = new UserKey(
                resultSet.getString("bot_id"),
                resultSet.getString("external_user_id"),
                resultSet.getString("chat_id")
        );
        Instant createdAt = resultSet.getTimestamp("user_created_at").toInstant();
        return new BotUser(
                resultSet.getObject("user_id", UUID.class),
                key,
                resultSet.getString("messenger"),
                UserStatus.valueOf(resultSet.getString("user_status")),
                createdAt
        );
    }
}
