package application.subscription;

import application.user.UserKey;

public class SubscriptionManager {
    SubscriptionClient client;
    NotificationQueue notificationQueue;


    public void track(Subscription subscription) {
        client.track(subscription);
    }


    public void untrack(Subscription subscription) {
        client.untrack(subscription);
    }


    public void list(UserKey userKey) {
        client.list(userKey);
    }


    public void stop(UserKey userKey) {
        client.stop(userKey);
    }


    public void delete(UserKey userKey) {
        client.delete(userKey);
    }


    public void putSubscriptions(
        Subscription[] subscriptions
    ) {
        client.putSubscriptions(subscriptions);
    }


    public void publishNotifications(
        Notification[] notifications
    ) throws InterruptedException {

        notificationQueue.publishNotifications(
            notifications
        );
    }


    public Notification[] takeNotifications()
        throws InterruptedException {

        return notificationQueue.take();
    }


    public String[] getAvailableSubscriptions() {
        return client.getAvailableSubscriptions();
    }
}
