package application;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public final class SubscriptionQueue {

    private final BlockingQueue<Subscription> subscriptions =
            new ArrayBlockingQueue<>(10_000);


    public void publishSubscription(Subscription subscription)
            throws InterruptedException {

        subscriptions.put(subscription);
    }


    public Subscription take() throws InterruptedException {
        return subscriptions.take();
    }


    public Subscription poll() {
        return subscriptions.poll();
    }
}