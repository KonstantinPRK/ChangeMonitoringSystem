package application.tracker;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

public record TrackerInstance(String trackerId, URI baseUrl, Set<String> supportedHosts, Instant availableUntil) {
    public TrackerInstance {
        supportedHosts = Set.copyOf(supportedHosts);
    }
}
