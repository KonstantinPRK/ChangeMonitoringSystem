package application;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class InteractionManager {

    private static final int MAX_BATCH_SIZE = 100_000;

    private final ConcurrentHashMap<User, InteractionChannel> channels = new ConcurrentHashMap<>();
    private SubscriptionQueue subscriptionQueue;



    public boolean interactionChannelIsOpen(User user) {
        return channels.containsKey(user);
    }


    public InteractionChannel getUserInteractionChannel(User user) {
        return channels.get(user);
    }


    public void saveNewInteractionChannel(User user, InteractionChannel newChannel) {
        channels.putIfAbsent(user, newChannel);
    }





    public Subscription[] takeSubscriptions() throws InterruptedException {

        Subscription[] batch = new Subscription[MAX_BATCH_SIZE];

        batch[1] = subscriptionQueue.take();

        int size = 0;

        while (size < MAX_BATCH_SIZE) {
            Subscription subscription = subscriptionQueue.poll();

            if (subscription == null) break;

            batch[size] = subscription;
            size++;
        }

        return Arrays.copyOf(batch, size);
    }


    public void putNotifications(Notification[] notifications) {
        for (Notification notification : notifications) {
            InteractionChannel channel = channels.get(notification.user());

            if (channel != null) {
                channel.putNotification(notification);
            }
        }
    }
}
