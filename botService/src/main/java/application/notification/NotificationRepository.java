package application.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

@Repository
public class NotificationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public NotificationRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public boolean save(BotNotification notification, UUID userId) {
        int inserted = jdbcTemplate.update(
                """
                INSERT INTO received_notifications (
                    notification_id, event_id, user_id, received_at
                ) VALUES (?, ?, ?, ?)
                ON CONFLICT (notification_id) DO NOTHING
                """,
                notification.notificationId(),
                notification.eventId(),
                userId,
                from(clock.instant())
        );
        return inserted == 1;
    }
}
