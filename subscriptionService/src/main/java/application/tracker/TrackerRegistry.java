package application.tracker;

import application.registration.RegistrationAvailabilityPolicy;
import application.registration.RegistrationValidator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Хранит и находит зарегистрированные компоненты через {@code TrackerRegistry}.
 */
@Component
public class TrackerRegistry {
    private final Map<String, TrackerInstance> trackersById = new HashMap<>();
    private final RegistrationValidator validator;
    private final TrackerHostRegistry hostOwnership;
    private final RegistrationAvailabilityPolicy availabilityPolicy;


    public TrackerRegistry(
        RegistrationValidator validator,
        Clock clock,
        @Value("${app.registration.availability-timeout:30s}")
        Duration availabilityTimeout
    ) {
        this.validator = validator;
        this.hostOwnership = new TrackerHostRegistry(validator);
        this.availabilityPolicy = new RegistrationAvailabilityPolicy(
            clock,
            availabilityTimeout
        );
    }


    public synchronized TrackerInstance register(
        String trackerId,
        URI baseUrl,
        Set<String> supportedHosts
    ) {
        TrackerInstance tracker = createValidatedTracker(
            trackerId,
            baseUrl,
            supportedHosts
        );

        replaceRegisteredTracker(tracker);

        return tracker;
    }


    public synchronized TrackerInstance confirmAvailability(String trackerId) {
        TrackerInstance registeredTracker = requireRegistered(trackerId);
        TrackerInstance availableTracker = new TrackerInstance(
            registeredTracker.trackerId(),
            registeredTracker.baseUrl(),
            registeredTracker.supportedHosts(),
            availabilityPolicy.nextDeadline()
        );

        trackersById.put(trackerId, availableTracker);

        return availableTracker;
    }


    public synchronized TrackerInstance requireAvailable(String trackerId) {
        TrackerInstance tracker = requireRegistered(trackerId);
        availabilityPolicy.requireAvailable(
            tracker.availableUntil(),
            "Tracker"
        );

        return tracker;
    }


    public synchronized TrackerInstance requireAvailableForHost(String host) {
        String trackerId = hostOwnership.requireTrackerIdForHost(host);
        TrackerInstance tracker = trackersById.get(trackerId);

        availabilityPolicy.requireAvailable(
            tracker.availableUntil(),
            "Tracker"
        );

        return tracker;
    }


    public synchronized void requireOwnership(String trackerId, String host) {
        TrackerInstance tracker = requireRegistered(trackerId);
        hostOwnership.requireOwnership(tracker.trackerId(), host);
    }


    public synchronized List<TrackerInstance> list() {
        List<TrackerInstance> registeredTrackers =
            new ArrayList<>(trackersById.values());

        registeredTrackers.sort(
            Comparator.comparing(TrackerInstance::trackerId)
        );

        return List.copyOf(registeredTrackers);
    }


    public synchronized List<String> supportedHosts() {
        return hostOwnership.supportedHosts();
    }


    public synchronized void remove(String trackerId) {
        TrackerInstance tracker = trackersById.remove(
            validator.identifier(trackerId)
        );

        if (tracker != null) hostOwnership.removeFromTracker(tracker);
    }


    private TrackerInstance requireRegistered(String trackerId) {
        TrackerInstance tracker = trackersById.get(
            validator.identifier(trackerId)
        );

        if (tracker == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Tracker is not registered"
            );
        }

        return tracker;
    }


    private TrackerInstance createValidatedTracker(
        String trackerId,
        URI baseUrl,
        Set<String> supportedHosts
    ) {
        String normalizedTrackerId = validator.identifier(trackerId);
        URI normalizedBaseUrl = validator.baseUrl(baseUrl);
        Set<String> normalizedHosts = validator.hosts(supportedHosts);

        hostOwnership.requireHostsAvailableForTracker(
            normalizedTrackerId,
            normalizedHosts
        );

        return new TrackerInstance(
            normalizedTrackerId,
            normalizedBaseUrl,
            normalizedHosts,
            availabilityPolicy.nextDeadline()
        );
    }


    private void replaceRegisteredTracker(TrackerInstance tracker) {
        TrackerInstance previousTracker = trackersById.put(
            tracker.trackerId(),
            tracker
        );

        if (previousTracker != null) {
            hostOwnership.removeFromTracker(previousTracker);
        }

        hostOwnership.assignToTracker(tracker);
    }
}
