package application;

import application.core.BotManager;
import application.core.CurrentSystemStatus;
import application.core.TrackerManager;
import infrastructure.http.SystemHttpServer;

import org.springframework.context.SmartLifecycle;

import java.util.concurrent.CountDownLatch;

public final class ChangeMonitoringSystem implements SmartLifecycle {
    private static final System.Logger LOGGER = System.getLogger(ChangeMonitoringSystem.class.getName());

    private final BotManager botManager;
    private final TrackerManager trackerManager;
    private final SystemHttpServer httpServer;
    private final CountDownLatch terminationLatch = new CountDownLatch(1);

    private volatile CurrentSystemStatus status;


    public ChangeMonitoringSystem(
        BotManager botManager,
        TrackerManager trackerManager,
        SystemHttpServer httpServer
    ) {
        this.botManager = botManager;
        this.trackerManager = trackerManager;
        this.httpServer = httpServer;

        this.status = CurrentSystemStatus.NEW;
    }


    @Override
    public synchronized void start() {
        if (status == CurrentSystemStatus.RUNNING) return;
        if (status != CurrentSystemStatus.NEW) {
            throw new IllegalStateException("Cannot start system from status: " + status);
        }

        status = CurrentSystemStatus.STARTING;

        try {
            botManager.start();
            trackerManager.start();
            httpServer.start();
            status = CurrentSystemStatus.RUNNING;

        } catch (RuntimeException exception) {
            status = CurrentSystemStatus.FAILED;
            stopStartedComponents();
            terminationLatch.countDown();

            throw exception;
        }
    }


    public void awaitTermination() throws InterruptedException {
        terminationLatch.await();
    }


    @Override
    public synchronized void stop() {
        if (status == CurrentSystemStatus.STOPPED
            || status == CurrentSystemStatus.STOPPING) {
            return;
        }

        status = CurrentSystemStatus.STOPPING;
        stopStartedComponents();
        status = CurrentSystemStatus.STOPPED;

        terminationLatch.countDown();
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


    private void stopStartedComponents() {
        stopSafely(httpServer.getClass().getName(), httpServer::stop);
        stopSafely(trackerManager.getClass().getName(), trackerManager::stop);
        stopSafely(botManager.getClass().getName(), botManager::stop);
    }


    private void stopSafely(String componentName, Runnable stopAction) {
        try {
            stopAction.run();

        } catch (RuntimeException exception) {
            LOGGER.log(
                System.Logger.Level.ERROR,
                "Cannot stop " + componentName,
                exception
            );

        }
    }
}
