package application.notification;

import application.link.LinkNotification;
import application.link.LinkNotificationQueue;
import application.metrics.ServiceMetrics;
import application.queue.QueueReceipt;
import application.user.User;
import application.user.SubscriberBatch;
import application.user.UserManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationManager {
    private static final int RECIPIENT_BATCH_SIZE = 500;
    private final LinkNotificationQueue updateQueue;
    private final NotificationFactory notificationFactory;
    private final NotificationQueue notificationQueue;
    private final UserManager userManager;
    private final ServiceMetrics metrics;


    public NotificationManager(
        LinkNotificationQueue updateQueue,
        NotificationFactory notificationFactory,
        NotificationQueue notificationQueue,
        UserManager userManager,
        ServiceMetrics metrics
    ) {
        this.updateQueue = updateQueue;
        this.notificationFactory = notificationFactory;
        this.notificationQueue = notificationQueue;
        this.userManager = userManager;
        this.metrics = metrics;
    }


    public QueueReceipt saveLinkNotification(LinkNotification notification) {
        QueueReceipt receipt = updateQueue.addToQueue(notification);

        metrics.acceptedUpdate();

        return receipt;
    }


    @Transactional
    public void process(LinkNotification notification) {
        long lastProcessedUserId = 0;
        SubscriberBatch subscribers;

        do {
            subscribers = userManager.getLinkSubscribers(
                notification.link(),
                lastProcessedUserId,
                RECIPIENT_BATCH_SIZE
            );

            enqueueNotifications(notification, subscribers.users());
            lastProcessedUserId = subscribers.lastUserId();

        } while (isFullBatch(subscribers));
    }


    private void enqueueNotifications(
        LinkNotification notification,
        Iterable<User> subscribers
    ) {
        for (User subscriber : subscribers) {
            notificationQueue.addToQueue(
                notificationFactory.create(notification, subscriber)
            );
        }
    }


    private boolean isFullBatch(SubscriberBatch subscribers) {
        return subscribers.users().size() == RECIPIENT_BATCH_SIZE;
    }
}
