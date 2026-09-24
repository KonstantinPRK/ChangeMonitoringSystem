package application;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationFactory {
    int batchSize = 1;
    ExecutorService executor;
    LinkToUsersDataBase linkToUsersDataBase;
    LinkNotificationQueue linkNotificationQueue;
    NotificationQueue notificationQueue;

    public void start(){
        linkNotificationQueue.start();
        executor.submit(this::distributeNotifications);
    }

    public void stop(){
        linkNotificationQueue.stop();
    }

    public void saveLinkNotifications(LinkNotification[] linkNotifications) {
        linkNotificationQueue.addToQueue(linkNotifications);
    }


    public Notification[] takeBotsNotification() {
        Notification[] notifications = new Notification[batchSize];
        for(int counter = 0; counter < batchSize; counter++) notifications[counter] = notificationQueue.takeNotification();
        return notifications;
    }

    private void distributeNotifications(){
        while(!Thread.currentThread().isInterrupted()){
            Link link = linkNotificationQueue.takeLinkNotification().link();
            String message = linkNotificationQueue.takeLinkNotification().message();

            Set<User> linkSubscribers = linkToUsersDataBase.getLinkSubscribers(link);
            for(User user : linkSubscribers) notificationQueue.addToQueue(new Notification(user, message));
        }
    }
}
