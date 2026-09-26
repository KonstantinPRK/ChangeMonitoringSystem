package application.notification.model;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code StoredNotification}.
 */
public record StoredNotification(
        long id,
        UUID eventId,
        long trackedResourceId,
        String payloadJson,
        int attempts,
        UUID claimToken
) {
}
