package application;

import application.lifecycle.TrackerServiceStatus;
import application.catalog.synchronization.LinkSynchronizationScheduler;
import application.notification.outbox.NotificationDispatcher;
import application.registration.TrackerRegistrationManager;
import application.tracking.ScrapeDispatcher;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class TrackerService implements SmartLifecycle {
    private final TrackerRegistrationManager registrationManager;
    private final LinkSynchronizationScheduler synchronizationScheduler;
    private final ScrapeDispatcher scrapeDispatcher;
    private final NotificationDispatcher notificationDispatcher;
    private final AtomicReference<TrackerServiceStatus> status = new AtomicReference<>(
            TrackerServiceStatus.NEW
    );


    public TrackerService(
            TrackerRegistrationManager registrationManager,
            LinkSynchronizationScheduler synchronizationScheduler,
            ScrapeDispatcher scrapeDispatcher,
            NotificationDispatcher notificationDispatcher
    ) {
        this.registrationManager = registrationManager;
        this.synchronizationScheduler = synchronizationScheduler;
        this.scrapeDispatcher = scrapeDispatcher;
        this.notificationDispatcher = notificationDispatcher;
    }


    @Override
    public synchronized void start() {
        if (!canStart()) return;
        status.set(TrackerServiceStatus.STARTING);

        try {
            startComponents();
            status.set(TrackerServiceStatus.RUNNING);

        } catch (RuntimeException exception) {
            stopStartedComponents();
            status.set(TrackerServiceStatus.STOPPED);
            throw exception;

        }
    }


    @Override
    public void stop() {
        stop(() -> { });
    }


    @Override
    public synchronized void stop(Runnable callback) {
        if (!canStop()) {
            callback.run();
            return;
        }

        status.set(TrackerServiceStatus.STOPPING);
        stopStartedComponents();
        registrationManager.stop().whenComplete((ignored, failure) -> completeStop(callback));
    }


    @Override
    public boolean isRunning() {
        return status.get() == TrackerServiceStatus.RUNNING;
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }


    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1024;
    }


    public TrackerServiceStatus status() {
        return status.get();
    }


    private boolean canStart() {
        TrackerServiceStatus currentStatus = status.get();
        return currentStatus == TrackerServiceStatus.NEW
                || currentStatus == TrackerServiceStatus.STOPPED;
    }


    private boolean canStop() {
        TrackerServiceStatus currentStatus = status.get();
        return currentStatus == TrackerServiceStatus.STARTING
                || currentStatus == TrackerServiceStatus.RUNNING;
    }


    private void startComponents() {
        notificationDispatcher.start();
        scrapeDispatcher.start();
        registrationManager.start();
        synchronizationScheduler.start();
    }


    private void stopStartedComponents() {
        synchronizationScheduler.stop();
        scrapeDispatcher.stop();
        notificationDispatcher.stop();
    }


    private void completeStop(Runnable callback) {
        status.set(TrackerServiceStatus.STOPPED);
        callback.run();
    }
}
