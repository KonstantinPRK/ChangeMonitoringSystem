package application.registration;

import java.net.URI;
import java.util.Set;

public record TrackerRegistration(
    String trackerId,
    String instanceId,
    String resourceProvider,
    URI baseUrl,
    int contractVersion,
    Set<String> supportedHosts,
    Set<String> capabilities
) {
    public static TrackerRegistration local(String instanceId, URI baseUrl) {
        return new TrackerRegistration(
            "GitHubTracker",
            instanceId,
            "GITHUB",
            baseUrl,
            1,
            Set.of("github.com"),
            Set.of("PULL_REQUEST", "ISSUE")
        );
    }
}

