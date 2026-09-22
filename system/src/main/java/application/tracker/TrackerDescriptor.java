package application.tracker;

import java.net.URI;
import java.util.Set;

public record TrackerDescriptor(
    String trackerId,
    String instanceId,
    String resourceProvider,
    URI baseUrl,
    int contractVersion,
    Set<String> supportedHosts,
    Set<String> capabilities
) {
}

