package application.subscription;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public final class SubscriptionQueue {
    private static final int QUEUE_CAPACITY = 10_000;

    private final BlockingQueue<Subscription> subscriptions =
        new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private int maxBatchSize;


    public void publishSubscription(Subscription subscription)
        throws InterruptedException {

        subscriptions.put(subscription);
    }


    public Subscription[] take() throws InterruptedException {
        Subscription[] subscriptionPortion =
            new Subscription[maxBatchSize];

        for (
            int batchIndex = 0;
            batchIndex < maxBatchSize;
            batchIndex++
        ) {
            subscriptionPortion[batchIndex] = subscriptions.take();
        }

        return subscriptionPortion;
    }
}
