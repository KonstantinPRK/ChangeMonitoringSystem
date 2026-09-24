package application;

import java.util.concurrent.LinkedTransferQueue;

public class NotificationQueue {
    LinkedTransferQueue<Notification> linkNotifications;


    public void addToQueue(Notification notification) {
        linkNotifications.add(notification);
    }

    public Notification takeNotification() {
        try {
            return linkNotifications.take();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
