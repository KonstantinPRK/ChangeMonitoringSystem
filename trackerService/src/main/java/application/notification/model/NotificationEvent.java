package application.notification.model;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code NotificationEvent}.
 */
public record NotificationEvent(UUID eventId, String payloadJson) {
}
