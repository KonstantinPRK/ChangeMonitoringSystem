package application.interaction;

import application.subscription.Notification;
import application.subscription.Subscription;
import application.subscription.SubscriptionQueue;
import application.user.User;
import application.user.UserManager;

import org.springframework.stereotype.Component;

@Component
public final class InteractionManager {
    private InteractionDataBase interactionDataBase;
    private SubscriptionQueue subscriptionQueue;
    private UserManager userManager;


    public boolean interactionChannelIsOpen(User user) {
        return interactionDataBase.interactionChannelIsOpen(
            user.key()
        );
    }


    public InteractionChannel getInteractionChannel(User user) {
        return interactionDataBase.getInteractionChannel(
            user.key()
        );
    }


    public void createNewInteractionChannel(User user) {
        interactionDataBase.saveNewInteractionChannel(
            user.key(),
            InteractionChannel.openNewInteractionChannel(user)
        );
    }


    public Subscription[] takeSubscriptions() throws InterruptedException {
        return subscriptionQueue.take();
    }


    public void putNotifications(Notification[] notifications)
        throws InterruptedException {

        for (Notification notification : notifications) {
            User user = userManager.getUser(
                notification.userKey()
            );

            interactionDataBase
                .getInteraction(user.key())
                .notify(notification.message());
        }
    }
}
