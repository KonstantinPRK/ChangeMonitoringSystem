package application.notification;

import application.link.LinkNotification;
import application.user.User;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Создаёт доменные или протокольные объекты через {@code NotificationFactory}.
 */
@Component
public class NotificationFactory {


    public Notification create(LinkNotification event, User user) {
        String recipient = event.eventId() + ":" + user.botId().length() + ":" + user.botId()
                + ":" + user.userId().length() + ":" + user.userId() + ":" + user.chatId();
        UUID notificationId = UUID.nameUUIDFromBytes(recipient.getBytes(StandardCharsets.UTF_8));
        return new Notification(notificationId, event.eventId(), user, event.link(), event.message(), event.occurredAt());
    }
}
