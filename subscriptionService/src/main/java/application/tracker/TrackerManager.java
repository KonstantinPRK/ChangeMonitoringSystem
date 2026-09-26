package application.tracker;

import application.link.LinkNotification;
import application.link.LinkNotificationManager;
import application.queue.QueueKind;
import application.queue.QueueReceipt;
import application.queue.QueueStore;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Координирует совместную работу компонентов через {@code TrackerManager}.
 */
@Service
public class TrackerManager {
    private final LinkNotificationManager notifications;
    private final QueueStore queue;


    public TrackerManager(LinkNotificationManager notifications, QueueStore queue) {
        this.notifications = notifications;
        this.queue = queue;
    }


    public QueueReceipt putLinkNotification(LinkNotification notification) {
        return notifications.saveLinkNotification(notification);
    }


    public QueueReceipt notificationStatus(UUID eventId) {
        return queue.status(QueueKind.LINK_NOTIFICATION, eventId);
    }
}
