package application.registration;

import java.net.URI;
import java.util.Set;

public record TrackerRegistrationRequest(String trackerId, URI baseUrl, Set<String> supportedHosts) {
}
