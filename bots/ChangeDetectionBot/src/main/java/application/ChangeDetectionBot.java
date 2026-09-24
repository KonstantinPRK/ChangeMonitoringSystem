package application;

import application.subscription.SubscriptionDataBridge;

import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class ChangeDetectionBot {
    private final SubscriptionDataBridge dataBridge;
    private final CountDownLatch terminationLatch = new CountDownLatch(1);


    public ChangeDetectionBot(SubscriptionDataBridge dataBridge) {
        this.dataBridge = dataBridge;
    }


    public void start() {
        try {
            dataBridge.start();

        } catch (RuntimeException exception) {
            terminationLatch.countDown();
            throw exception;

        }
    }


    public void awaitTermination() throws InterruptedException {
        terminationLatch.await();
    }


    public void stop() {
        try {
            dataBridge.stop();

        } finally {
            terminationLatch.countDown();

        }
    }
}
