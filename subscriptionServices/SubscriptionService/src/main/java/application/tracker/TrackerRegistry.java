package application.tracker;

import application.registration.RemoteSystemStatus;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TrackerRegistry {
    private final ConcurrentMap<String, TrackerInstance> trackersById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> trackerIdBySupportedHost = new ConcurrentHashMap<>();


    public synchronized void register(TrackerInstance tracker) {
        for (String host : tracker.supportedHosts()) {
            String owner = trackerIdBySupportedHost.get(host);
            if (owner != null && !owner.equals(tracker.trackerId())) {
                throw new IllegalStateException("Host " + host + " is already handled by tracker " + owner);
            }
        }

        TrackerInstance previous = trackersById.put(tracker.trackerId(), tracker);
        if (previous != null) {
            for (String host : previous.supportedHosts()) {
                trackerIdBySupportedHost.remove(host, previous.trackerId());
            }
        }

        for (String host : tracker.supportedHosts()) {
            trackerIdBySupportedHost.put(host, tracker.trackerId());
        }
    }


    public TrackerInstance confirmAvailability(String trackerId, Instant confirmedUntil) {
        TrackerInstance updated = trackersById.computeIfPresent(
            requireId(trackerId),
            (id, tracker) -> tracker.confirmAvailability(confirmedUntil)
        );

        if (updated == null) {
            throw new IllegalArgumentException("Unknown tracker: " + trackerId);
        }

        return updated;
    }


    public synchronized void unregister(String trackerId) {
        TrackerInstance removed = trackersById.remove(requireId(trackerId));
        if (removed != null) {
            for (String host : removed.supportedHosts()) {
                trackerIdBySupportedHost.remove(host, removed.trackerId());
            }
        }
    }


    public Optional<TrackerInstance> find(String trackerId) {
        return Optional.ofNullable(trackersById.get(requireId(trackerId)));
    }


    public Optional<TrackerInstance> findActiveBySupportedHost(String host, Instant now) {
        String trackerId = trackerIdBySupportedHost.get(normalizeHost(host));
        if (trackerId == null) return Optional.empty();

        return find(trackerId).filter(tracker -> isActive(tracker, now));
    }


    public List<TrackerInstance> findAll() {
        return List.copyOf(trackersById.values());
    }


    public void markExpiredUnavailable(Instant now) {
        trackersById.replaceAll((id, tracker) -> {
            if (tracker.status() == RemoteSystemStatus.ACTIVE
                && !tracker.availabilityConfirmedUntil().isAfter(now)) {
                return tracker.markUnavailable();
            }

            return tracker;
        });
    }


    public int size() {
        return trackersById.size();
    }


    private static boolean isActive(TrackerInstance tracker, Instant now) {
        return tracker.status() == RemoteSystemStatus.ACTIVE
            && tracker.availabilityConfirmedUntil().isAfter(now);
    }


    private static String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }

        return host.strip().toLowerCase(Locale.ROOT);
    }


    private static String requireId(String trackerId) {
        if (trackerId == null || trackerId.isBlank()) {
            throw new IllegalArgumentException("trackerId must not be blank");
        }

        return trackerId.strip();
    }
}
