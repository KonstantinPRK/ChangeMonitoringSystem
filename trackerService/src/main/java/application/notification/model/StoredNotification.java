package application.notification.model;

import java.util.UUID;

public record StoredNotification(
        long id,
        UUID eventId,
        long trackedResourceId,
        String payloadJson,
        int attempts,
        UUID claimToken
) {
}
