package application;

public class SubscriptionManager {
    NotificationFactory notificationFactory;
    LinkRequestFactory linkRequestFactory;


    public void putSubscriptionRequests(SubscriptionRequest[] requests) {
        userManager.saveSubscriptionRequests(requests);
    }

    public void putLinkNotifications(LinkNotification[] linkNotifications) {
        notificationFactory.saveLinkNotifications(linkNotifications);
    }



    public Notification[] takeBotsNotifications() {
        return notificationFactory.takeBotsNotification();
    }

    public LinkRequest[] takeLinkRequests() {
        return userManager.takeLinkRequests();
    }


}
