package application.core;

import application.tracker.TrackerInstance;

import java.net.URI;

public final class TrackingCoordinator {
    private final BotManager botManager;
    private final TrackerManager trackerManager;
    private final TrackerAccessPolicy accessPolicy;


    public TrackingCoordinator(
        BotManager botManager,
        TrackerManager trackerManager,
        TrackerAccessPolicy accessPolicy
    ) {
        this.botManager = botManager;
        this.trackerManager = trackerManager;
        this.accessPolicy = accessPolicy;
    }


    public void addSubscription(String botId, String localUserId, URI resource) {
        botManager.findAvailable(botId);
        TrackerInstance tracker = trackerManager.route(resource);
        accessPolicy.requireAllowed(botId, tracker.trackerId());
        trackerManager.addSubscription(tracker, resource, botId, localUserId);
    }
}
