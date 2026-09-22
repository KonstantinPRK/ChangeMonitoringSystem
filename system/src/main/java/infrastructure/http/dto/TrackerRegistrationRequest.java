package infrastructure.http.dto;

import java.net.URI;
import java.util.Set;

public record TrackerRegistrationRequest(
    String trackerId,
    String resourceProvider,
    URI baseUrl,
    int contractVersion,
    Set<String> supportedHosts,
    Set<String> capabilities
) {
}
