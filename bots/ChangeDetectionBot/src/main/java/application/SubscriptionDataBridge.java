package application;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public final class SubscriptionDataBridge {
    private InteractionManager interactionManager;
    private SubscriptionManager subscriptionManager;

    private ExecutorService executor;


    public void start() {
        executor.submit(this::forwardNotifications);
        executor.submit(this::forwardSubscriptions);
    }

    public void stop() {
        executor.shutdown();
    }


    private void forwardNotifications() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Notification[] notifications = subscriptionManager.takeNotifications();
                interactionManager.putNotifications(notifications);

            }
        } catch (Exception exception) {
            Thread.currentThread().interrupt();

        }
    }


    private void forwardSubscriptions() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Subscription[] subscriptions = interactionManager.takeSubscriptions();
                subscriptionManager.putSubscriptions(subscriptions);

            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


}
