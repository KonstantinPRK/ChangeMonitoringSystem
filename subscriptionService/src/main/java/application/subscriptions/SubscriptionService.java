package application.subscriptions;

import application.queue.QueueProcessor;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

/**
 * Управляет жизненным циклом сервиса подписок.
 */
@Component
public class SubscriptionService implements SmartLifecycle {
    private final QueueProcessor processor;
    private final CountDownLatch termination = new CountDownLatch(1);

    private volatile CurrentSystemStatus status = CurrentSystemStatus.NEW;


    public SubscriptionService(QueueProcessor processor) {
        this.processor = processor;
    }


    @Override
    public synchronized void start() {
        if (isRunning()) return;
        status = CurrentSystemStatus.STARTING;
        processor.start();
        status = CurrentSystemStatus.RUNNING;
    }


    @Override
    public synchronized void stop() {
        if (!isRunning()) return;
        status = CurrentSystemStatus.STOPPING;
        processor.stop();
        status = CurrentSystemStatus.STOPPED;
        termination.countDown();
    }


    @Override
    public void stop(Runnable callback) {
        try {
            stop();

        } finally {
            callback.run();

        }
    }


    @Override
    public boolean isRunning() {
        return status == CurrentSystemStatus.RUNNING;
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }


    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1024;
    }


    public CurrentSystemStatus status() {
        return status;
    }


    public void awaitTermination() throws InterruptedException {
        termination.await();
    }
}
