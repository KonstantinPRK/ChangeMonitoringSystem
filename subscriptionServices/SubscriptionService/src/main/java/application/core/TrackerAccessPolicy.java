package application.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TrackerAccessPolicy {
    private final ConcurrentMap<String, Set<String>> restrictionsByBotId =
        new ConcurrentHashMap<>();


    public boolean isAllowed(String botId, String trackerId) {
        String checkedBotId = requireId(botId, "botId");
        String checkedTrackerId = requireId(trackerId, "trackerId");
        Set<String> allowedTrackers = restrictionsByBotId.get(checkedBotId);

        return allowedTrackers == null || allowedTrackers.contains(checkedTrackerId);
    }


    public void allowOnly(String botId, Set<String> trackerIds) {
        Set<String> checkedTrackerIds = new HashSet<>();
        for (String trackerId : trackerIds) {
            checkedTrackerIds.add(requireId(trackerId, "trackerId"));
        }

        restrictionsByBotId.put(
            requireId(botId, "botId"),
            Set.copyOf(checkedTrackerIds)
        );
    }


    public void allowAll(String botId) {
        restrictionsByBotId.remove(requireId(botId, "botId"));
    }


    public void requireAllowed(String botId, String trackerId) {
        if (!isAllowed(botId, trackerId)) {
            throw new IllegalStateException(
                "Bot " + botId + " is not allowed to use tracker " + trackerId
            );
        }
    }


    private static String requireId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.strip();
    }
}
