package application.tracker;

import java.net.URI;

public interface TrackerClient {
    void addSubscription(
        TrackerInstance tracker,
        URI resource,
        String botId,
        String localUserId
    );


    default void close() {
    }
}
