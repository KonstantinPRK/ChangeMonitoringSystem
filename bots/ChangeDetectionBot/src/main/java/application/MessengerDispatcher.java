package application;

import java.util.concurrent.ExecutorService;

public final class MessengerDispatcher {
    private InteractionManager interactionManager;
    private SubscriptionManager subscriptionManager;

    private ExecutorService executor;



    public void working() {
        executor.submit(this::forwardNotifications);
        executor.submit(this::forwardSubscriptions);
    }


    public void stop() {
        executor.shutdownNow();
    }


    private void forwardNotifications() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Notification[] notifications =
                        subscriptionManager.takeNotifications();

                interactionManager.putNotifications(notifications);
            }
        } catch (Exception exception) {
            Thread.currentThread().interrupt();
        }
    }


    private void forwardSubscriptions() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Subscription[] subscriptions =
                        interactionManager.takeSubscriptions();

                subscriptionManager.putSubscriptions(subscriptions);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    public void start() {
    }
}
