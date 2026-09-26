package application.tracker;

import application.registration.RegistrationValidator;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Хранит принадлежность поддерживаемых хостов зарегистрированным трекерам.
 */
class TrackerHostRegistry {
    private final Map<String, String> trackerIdsByHost = new HashMap<>();
    private final RegistrationValidator validator;


    TrackerHostRegistry(RegistrationValidator validator) {
        this.validator = validator;
    }


    void requireHostsAvailableForTracker(
        String trackerId,
        Set<String> hosts
    ) {
        for (String host : hosts) {
            String ownerTrackerId = trackerIdsByHost.get(host);

            if (belongsToAnotherTracker(ownerTrackerId, trackerId)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Host is already assigned to another tracker: " + host
                );
            }
        }
    }


    void assignToTracker(TrackerInstance tracker) {
        for (String host : tracker.supportedHosts()) {
            trackerIdsByHost.put(host, tracker.trackerId());
        }
    }


    void removeFromTracker(TrackerInstance tracker) {
        for (String host : tracker.supportedHosts()) {
            trackerIdsByHost.remove(host, tracker.trackerId());
        }
    }


    String requireTrackerIdForHost(String host) {
        String trackerId = trackerIdsByHost.get(
            validator.host(host)
        );

        if (trackerId == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No tracker supports this host"
            );
        }

        return trackerId;
    }


    void requireOwnership(String trackerId, String host) {
        String ownerTrackerId = trackerIdsByHost.get(
            validator.host(host)
        );

        if (!trackerId.equals(ownerTrackerId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Tracker does not own this host"
            );
        }
    }


    List<String> supportedHosts() {
        List<String> hosts = new ArrayList<>(trackerIdsByHost.keySet());
        hosts.sort(String::compareTo);

        return List.copyOf(hosts);
    }


    private boolean belongsToAnotherTracker(
        String ownerTrackerId,
        String registeringTrackerId
    ) {
        return ownerTrackerId != null
            && !ownerTrackerId.equals(registeringTrackerId);
    }
}
