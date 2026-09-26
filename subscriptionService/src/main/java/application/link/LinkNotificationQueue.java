package application.link;

import application.config.JsonCodec;
import application.queue.QueueKind;
import application.queue.QueueReceipt;
import application.queue.QueueStore;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Хранит ожидающие обработки элементы в {@code LinkNotificationQueue}.
 */
@Component
public class LinkNotificationQueue {
    private final QueueStore queue;
    private final JsonCodec json;


    public LinkNotificationQueue(QueueStore queue, JsonCodec json) {
        this.queue = queue;
        this.json = json;
    }


    public QueueReceipt addToQueue(LinkNotification notification) {
        String stream = UUID.nameUUIDFromBytes(notification.link().address().getBytes(StandardCharsets.UTF_8)).toString();
        return queue.publish(QueueKind.LINK_NOTIFICATION, notification.eventId(), stream, json.write(notification));
    }
}
