package application.subscription.operation;

import application.persistence.JsonValues;
import application.subscription.model.Link;
import application.user.BotUser;
import application.user.UserKey;
import application.user.UserStatus;

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
 * Отвечает за сохранение и чтение данных {@code SubscriptionOperationRepository}.
 */
@Repository
public class SubscriptionOperationRepository {
    private static final Duration CLAIM_LEASE = Duration.ofSeconds(60);
    private final JdbcClient jdbcClient;
    private final JsonValues jsonValues;
    private final Clock clock;


    public SubscriptionOperationRepository(JdbcClient jdbcClient, JsonValues jsonValues, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.jsonValues = jsonValues;
        this.clock = clock;
    }


    public void save(UUID operationId, BotUser user, SubscriptionOperationDraft draft) {
        Link link = draft.link();
        Instant now = clock.instant();

        jdbcClient.sql("""
                INSERT INTO subscription_operations (
                    id, operation_type, user_id, link_domain, link_address,
                    tags_json, filters_json, status, available_at, created_at
                ) VALUES (
                    :id, :operationType, :userId, :linkDomain, :linkAddress,
                    :tags, :filters, 'PENDING', :availableAt, :createdAt
                )
                ON CONFLICT (id) DO NOTHING
                """)
                .param("id", operationId)
                .param("operationType", draft.type().name())
                .param("userId", user.id())
                .param("linkDomain", link == null ? null : link.domain())
                .param("linkAddress", link == null ? null : link.address())
                .param("tags", jsonValues.writeStrings(draft.tags()))
                .param("filters", jsonValues.writeStrings(draft.filters()))
                .param("availableAt", from(now))
                .param("createdAt", from(now))
                .update();
    }


    public Optional<StoredSubscriptionOperation> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();

        return jdbcClient.sql("""
                WITH claimed AS (
                    UPDATE subscription_operations
                    SET status = 'PROCESSING', attempts = attempts + 1,
                        locked_until = :lockedUntil, claim_token = :claimToken
                    WHERE id = (
                        SELECT id FROM subscription_operations
                        WHERE (status IN ('PENDING', 'RETRY') AND available_at <= :now)
                           OR (status = 'PROCESSING' AND locked_until <= :now)
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
                """)
                .param("lockedUntil", from(now.plus(CLAIM_LEASE)))
                .param("claimToken", claimToken)
                .param("now", from(now))
                .query(this::mapOperation)
                .optional();
    }


    public void complete(StoredSubscriptionOperation operation) {
        jdbcClient.sql("""
                UPDATE subscription_operations
                SET status = 'COMPLETED', locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("id", operation.id())
                .param("claimToken", operation.claimToken())
                .update();
    }


    public void retry(StoredSubscriptionOperation operation, Duration delay, String error) {
        jdbcClient.sql("""
                UPDATE subscription_operations
                SET status = 'RETRY', available_at = :availableAt, last_error = :error,
                    locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("availableAt", from(clock.instant().plus(delay)))
                .param("error", error)
                .param("id", operation.id())
                .param("claimToken", operation.claimToken())
                .update();
    }


    public void fail(StoredSubscriptionOperation operation, String error) {
        jdbcClient.sql("""
                UPDATE subscription_operations
                SET status = 'FAILED', last_error = :error, locked_until = NULL, claim_token = NULL
                WHERE id = :id AND claim_token = :claimToken
                """)
                .param("error", error)
                .param("id", operation.id())
                .param("claimToken", operation.claimToken())
                .update();
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
