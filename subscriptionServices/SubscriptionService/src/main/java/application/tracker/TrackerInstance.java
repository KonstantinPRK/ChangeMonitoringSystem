package application.tracker;

import application.registration.RemoteSystemStatus;

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public record TrackerInstance(
    String trackerId,
    String resourceProvider,
    URI baseUrl,
    int contractVersion,
    Set<String> supportedHosts,
    Set<String> capabilities,
    RemoteSystemStatus status,
    Instant availabilityConfirmedUntil
) {
    public TrackerInstance {
        trackerId = requireText(trackerId, "trackerId");
        resourceProvider = requireText(resourceProvider, "resourceProvider");
        baseUrl = requireAbsoluteUri(baseUrl, "baseUrl");
        if (contractVersion < 1) {
            throw new IllegalArgumentException("contractVersion must be positive");
        }
        supportedHosts = normalizeHosts(supportedHosts);
        if (supportedHosts.isEmpty()) {
            throw new IllegalArgumentException("supportedHosts must not be empty");
        }

        capabilities = Set.copyOf(capabilities);
    }


    public TrackerInstance confirmAvailability(Instant confirmedUntil) {
        return new TrackerInstance(
            trackerId,
            resourceProvider,
            baseUrl,
            contractVersion,
            supportedHosts,
            capabilities,
            RemoteSystemStatus.ACTIVE,
            confirmedUntil
        );
    }


    public TrackerInstance markUnavailable() {
        return new TrackerInstance(
            trackerId,
            resourceProvider,
            baseUrl,
            contractVersion,
            supportedHosts,
            capabilities,
            RemoteSystemStatus.UNAVAILABLE,
            availabilityConfirmedUntil
        );
    }


    private static Set<String> normalizeHosts(Set<String> hosts) {
        Set<String> normalizedHosts = new HashSet<>();
        for (String host : hosts) {
            String normalizedHost = requireText(host, "supportedHost")
                .toLowerCase(Locale.ROOT);
            normalizedHosts.add(normalizedHost);
        }

        return Set.copyOf(normalizedHosts);
    }


    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.strip();
    }


    private static URI requireAbsoluteUri(URI value, String fieldName) {
        if (value == null || !value.isAbsolute() || value.getHost() == null) {
            throw new IllegalArgumentException(fieldName + " must be an absolute network URI");
        }

        return value;
    }
}
