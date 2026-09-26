package application.notification;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code NotificationRepository}.
 */
@Repository
public class NotificationRepository {
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public NotificationRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public boolean save(BotNotification notification, UUID userId) {
        int inserted = jdbcClient.sql("""
                INSERT INTO received_notifications (
                    notification_id, event_id, user_id, received_at
                ) VALUES (:notificationId, :eventId, :userId, :receivedAt)
                ON CONFLICT (notification_id) DO NOTHING
                """)
                .param("notificationId", notification.notificationId())
                .param("eventId", notification.eventId())
                .param("userId", userId)
                .param("receivedAt", from(clock.instant()))
                .update();

        return inserted == 1;
    }
}
