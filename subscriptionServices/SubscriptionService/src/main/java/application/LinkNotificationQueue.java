package application;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;

public class LinkNotificationQueue {
    ExecutorService executor;
    LinkedTransferQueue<LinkNotification[]> linkNotificationsPendingAdditions;
    LinkedTransferQueue<LinkNotification> linkNotifications;

    public void start() {
        executor.submit(this::forwardInPermanentQueue);
    }

    public void stop() {
        //save очереди
    }

    public void addToQueue(LinkNotification[] linkNotifications) {
        linkNotificationsPendingAdditions.put(linkNotifications);
    }

    public LinkNotification takeLinkNotification() {
        try {
            return linkNotifications.take();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }

    private void forwardInPermanentQueue() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // take() может выбросить InterruptedException, если поток прерывают
                for (LinkNotification linkNotification : linkNotificationsPendingAdditions.take())
                    linkNotifications.put(linkNotification);

            }
        } catch (InterruptedException e) {
            // Важно: восстанавливаем статус прерывания потока
            Thread.currentThread().interrupt();
        }
    }
}
