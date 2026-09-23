package application.core;

import application.tracker.TrackerAvailabilityMonitor;
import application.tracker.TrackerClient;
import application.tracker.TrackerInstance;
import application.tracker.TrackerRegistry;
import application.tracker.TrackerRouter;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class TrackerManager {
    private final TrackerRegistry registry;
    private final TrackerRouter router;
    private final TrackerClient client;
    private final TrackerAvailabilityMonitor availabilityMonitor;
    private final Clock clock;
    private final Duration availabilityTimeout;

    private volatile boolean running;


    public TrackerManager(
        TrackerRegistry registry,
        TrackerRouter router,
        TrackerClient client,
        TrackerAvailabilityMonitor availabilityMonitor,
        Clock clock,
        Duration availabilityTimeout
    ) {
        this.registry = registry;
        this.router = router;
        this.client = client;
        this.availabilityMonitor = availabilityMonitor;
        this.clock = clock;
        this.availabilityTimeout = requirePositive(
            availabilityTimeout,
            "availabilityTimeout"
        );
    }


    public synchronized void start() {
        if (running) return;

        availabilityMonitor.start();
        running = true;
    }


    public synchronized void stop() {
        if (!running) return;

        running = false;
        availabilityMonitor.stop();
        client.close();
    }


    public void register(TrackerInstance tracker) {
        ensureRunning();
        registry.register(tracker.confirmAvailability(nextAvailabilityDeadline()));
    }


    public void confirmAvailability(String trackerId) {
        ensureRunning();
        registry.confirmAvailability(trackerId, nextAvailabilityDeadline());
    }


    public void unregister(String trackerId) {
        registry.unregister(trackerId);
    }


    public TrackerInstance route(URI resource) {
        ensureRunning();
        return router.route(resource);
    }


    public void addSubscription(
        TrackerInstance tracker,
        URI resource,
        String botId,
        String localUserId
    ) {
        ensureRunning();
        client.addSubscription(tracker, resource, botId, localUserId);
    }


    public List<TrackerInstance> findAll() {
        return registry.findAll();
    }


    public boolean isRunning() {
        return running;
    }


    private Instant nextAvailabilityDeadline() {
        return clock.instant().plus(availabilityTimeout);
    }


    private void ensureRunning() {
        if (!running) throw new IllegalStateException("TrackerManager is not running");
    }


    private static Duration requirePositive(Duration value, String fieldName) {
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }

        return value;
    }
}
