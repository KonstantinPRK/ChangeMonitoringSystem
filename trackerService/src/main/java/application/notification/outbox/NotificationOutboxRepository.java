package application.notification.outbox;

import application.config.WorkerProperties;
import application.notification.model.NotificationEvent;
import application.notification.model.StoredNotification;

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

import static application.transaction.DatabaseTime.from;

@Repository
public class NotificationOutboxRepository {
    private final JdbcTemplate jdbcTemplate;
    private final WorkerProperties workerProperties;
    private final Clock clock;


    public NotificationOutboxRepository(
            JdbcTemplate jdbcTemplate,
            WorkerProperties workerProperties,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.workerProperties = workerProperties;
        this.clock = clock;
    }


    public void save(long trackedResourceId, NotificationEvent event) {
        jdbcTemplate.update(
                """
                INSERT INTO notification_outbox (
                    event_id, tracked_resource_id, payload_json, status, available_at, created_at
                ) VALUES (?, ?, CAST(? AS jsonb), 'PENDING', ?, ?)
                ON CONFLICT (event_id) DO NOTHING
                """,
                event.eventId(),
                trackedResourceId,
                event.payloadJson(),
                from(clock.instant()),
                from(clock.instant())
        );
    }


    public Optional<StoredNotification> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();
        List<StoredNotification> notifications = jdbcTemplate.query(
                """
                UPDATE notification_outbox current
                SET status = 'PROCESSING', attempts = attempts + 1,
                    locked_until = ?, claim_token = ?
                WHERE current.id = (
                    SELECT candidate.id FROM notification_outbox candidate
                    WHERE ((candidate.status IN ('PENDING', 'RETRY') AND candidate.available_at <= ?)
                        OR (candidate.status = 'PROCESSING' AND candidate.locked_until <= ?))
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
                """,
                this::mapNotification,
                from(now.plus(workerProperties.claimLease())),
                claimToken,
                from(now),
                from(now)
        );
        return notifications.stream().findFirst();
    }


    public void complete(StoredNotification notification) {
        jdbcTemplate.update(
                """
                UPDATE notification_outbox
                SET status = 'COMPLETED', completed_at = ?, locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                from(clock.instant()),
                notification.id(),
                notification.claimToken()
        );
    }


    public void retry(StoredNotification notification, Duration delay, String error) {
        jdbcTemplate.update(
                """
                UPDATE notification_outbox
                SET status = 'RETRY', available_at = ?, last_error = ?,
                    locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                from(clock.instant().plus(delay)),
                error,
                notification.id(),
                notification.claimToken()
        );
    }


    public void fail(StoredNotification notification, String error) {
        jdbcTemplate.update(
                """
                UPDATE notification_outbox
                SET status = 'FAILED', last_error = ?, locked_until = NULL, claim_token = NULL
                WHERE id = ? AND claim_token = ?
                """,
                error,
                notification.id(),
                notification.claimToken()
        );
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
