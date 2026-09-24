package application;

import org.springframework.stereotype.Component;


@Component
public final class InteractionManager {
    private InteractionDataBase interactionDataBase;
    private SubscriptionQueue subscriptionQueue;


    public boolean interactionChannelIsOpen(User user) {
        return interactionDataBase.interactionChannelIsOpen(user);
    }


    public InteractionChannel getInteractionChannel(User user) {
        return interactionDataBase.getInteractionChannel(user);
    }


    public void createNewInteractionChannel(User user) {
        interactionDataBase.saveNewInteractionChannel(user, InteractionChannel.openNewInteractionChannel(user));
    }


    public Subscription[] takeSubscriptions() throws InterruptedException {
        return subscriptionQueue.take();
    }


    public void putNotifications(Notification[] notifications) throws InterruptedException {
        for (Notification notification : notifications) {
                    interactionDataBase
                            .getInteraction(notification.user())
                            .notify(notification.message());
        }
    }
}
