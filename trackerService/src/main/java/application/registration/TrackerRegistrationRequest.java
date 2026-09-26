package application.registration;

import java.net.URI;
import java.util.Set;

/**
 * Передаёт между компонентами данные {@code TrackerRegistrationRequest}.
 */
public record TrackerRegistrationRequest(String trackerId, URI baseUrl, Set<String> supportedHosts) {
}
