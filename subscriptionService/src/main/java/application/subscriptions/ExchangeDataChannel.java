package application.subscriptions;

import application.link.LinkNotification;
import application.notification.NotificationManager;
import application.queue.QueueReceipt;

import org.springframework.stereotype.Component;

@Component
public class ExchangeDataChannel {
    private final SubscriptionManager subscriptions;
    private final NotificationManager notifications;


    public ExchangeDataChannel(SubscriptionManager subscriptions, NotificationManager notifications) {
        this.subscriptions = subscriptions;
        this.notifications = notifications;
    }


    public QueueReceipt putSubscriptionRequest(SubscriptionRequest request) {
        return subscriptions.saveSubscriptionRequest(request);
    }


    public void processSubscription(SubscriptionRequest request) {
        subscriptions.process(request);
    }


    public QueueReceipt putLinkNotification(LinkNotification notification) {
        return notifications.saveLinkNotification(notification);
    }


    public void processNotification(LinkNotification notification) {
        notifications.process(notification);
    }
}
