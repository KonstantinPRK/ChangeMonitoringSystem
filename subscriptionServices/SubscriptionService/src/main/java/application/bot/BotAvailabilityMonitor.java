package application.bot;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BotAvailabilityMonitor {
    private static final System.Logger LOGGER = System.getLogger(
        BotAvailabilityMonitor.class.getName()
    );

    private final BotRegistry registry;
    private final Clock clock;
    private final Duration scanInterval;

    private ScheduledExecutorService scheduler;


    public BotAvailabilityMonitor(
        BotRegistry registry,
        Clock clock,
        Duration scanInterval
    ) {
        this.registry = registry;
        this.clock = clock;
        this.scanInterval = requirePositive(scanInterval, "scanInterval");
    }


    public synchronized void start() {
        if (scheduler != null) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "bot-availability-monitor");
            thread.setDaemon(false);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(
            this::scanSafely,
            0,
            scanInterval.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }


    public void stop() {
        ScheduledExecutorService currentScheduler;
        synchronized (this) {
            currentScheduler = scheduler;
            scheduler = null;
        }

        if (currentScheduler == null) return;

        currentScheduler.shutdown();
        try {
            boolean terminated = currentScheduler.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated) currentScheduler.shutdownNow();

        } catch (InterruptedException exception) {
            currentScheduler.shutdownNow();
            Thread.currentThread().interrupt();

        }
    }


    private void scanSafely() {
        try {
            registry.markExpiredUnavailable(clock.instant());

        } catch (RuntimeException exception) {
            LOGGER.log(System.Logger.Level.ERROR, "Bot availability scan failed", exception);

        }
    }


    private static Duration requirePositive(Duration value, String fieldName) {
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }

        return value;
    }
}
