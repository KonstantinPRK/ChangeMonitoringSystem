package application.subscription;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class NotificationQueue {
    private static final int QUEUE_CAPACITY = 10_000;

    private final BlockingQueue<Notification> notifications =
        new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private int maxBatchSize;


    public void publishNotification(Notification notification)
        throws InterruptedException {

        notifications.put(notification);
    }


    public void publishNotifications(Notification[] notificationPortion)
        throws InterruptedException {

        for (Notification notification : notificationPortion) {
            notifications.put(notification);
        }
    }


    public Notification[] take() throws InterruptedException {
        Notification[] notificationPortion =
            new Notification[maxBatchSize];

        for (
            int batchIndex = 0;
            batchIndex < maxBatchSize;
            batchIndex++
        ) {
            notificationPortion[batchIndex] =
                notifications.take();
        }

        return notificationPortion;
    }
}
