package application.notification;

import application.config.JsonCodec;
import application.queue.QueueKind;
import application.queue.QueueStore;

import org.springframework.stereotype.Component;

/**
 * Хранит ожидающие обработки элементы в {@code NotificationQueue}.
 */
@Component
public class NotificationQueue {
    private final QueueStore queue;
    private final JsonCodec json;


    public NotificationQueue(QueueStore queue, JsonCodec json) {
        this.queue = queue;
        this.json = json;
    }


    public void addToQueue(Notification notification) {
        queue.publish(QueueKind.BOT_NOTIFICATION, notification.notificationId(), json.write(notification.user()), json.write(notification));
    }
}
