package application.tracker;

import java.net.URI;

public interface TrackerClient {
    void addSubscription(
        TrackerDescriptor tracker,
        URI resource,
        String botId,
        String localUserId
    );
}

