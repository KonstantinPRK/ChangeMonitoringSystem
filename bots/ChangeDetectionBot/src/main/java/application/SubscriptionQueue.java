package application;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public final class SubscriptionQueue {
    private int MAX_BATCH_SIZE;

    private final BlockingQueue<Subscription> subscriptions = new ArrayBlockingQueue<>(10_000);


    public void publishSubscription(Subscription subscription) throws InterruptedException {
        subscriptions.put(subscription);
    }


    public Subscription[] take() throws InterruptedException {
        Subscription[] subscriptionPortion = new Subscription[MAX_BATCH_SIZE];

        for(int batchCount = 0; batchCount < MAX_BATCH_SIZE; batchCount++ )
            subscriptionPortion[batchCount] = subscriptions.take();

        return subscriptionPortion;
    }

}