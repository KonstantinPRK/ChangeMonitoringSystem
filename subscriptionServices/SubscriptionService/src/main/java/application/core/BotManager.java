package application.core;

import application.bot.BotAvailabilityMonitor;
import application.bot.BotClient;
import application.bot.BotInstance;
import application.bot.BotRegistry;
import application.bot.BotRouter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class BotManager {
    private final BotRegistry registry;
    private final BotRouter router;
    private final BotClient client;
    private final BotAvailabilityMonitor availabilityMonitor;
    private final Clock clock;
    private final Duration availabilityTimeout;

    private volatile boolean running;


    public BotManager(
        BotRegistry registry,
        BotRouter router,
        BotClient client,
        BotAvailabilityMonitor availabilityMonitor,
        Clock clock,
        Duration availabilityTimeout
    ) {
        this.registry = registry;
        this.router = router;
        this.client = client;
        this.availabilityMonitor = availabilityMonitor;
        this.clock = clock;
        this.availabilityTimeout = availabilityTimeout;
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


    public void register(BotInstance bot) {
        ensureRunning();
        registry.register(bot.confirmAvailability(nextAvailabilityDeadline()));
    }


    public void confirmAvailability(String botId) {
        ensureRunning();
        registry.confirmAvailability(botId, nextAvailabilityDeadline());
    }


    public void unregister(String botId) {
        registry.unregister(botId);
    }


    public void sendNotification(String botId, String localUserId, String text) {
        ensureRunning();
        BotInstance selectedBot = router.route(botId);
        client.sendNotification(selectedBot, localUserId, text);
    }


    public BotInstance findAvailable(String botId) {
        ensureRunning();
        return router.route(botId);
    }


    public List<BotInstance> findAll() {
        return registry.findAll();
    }


    public boolean isRunning() {
        return running;
    }


    private Instant nextAvailabilityDeadline() {
        return clock.instant().plus(availabilityTimeout);
    }


    private void ensureRunning() {
        if (!running) throw new IllegalStateException("BotManager is not running");
    }
}
