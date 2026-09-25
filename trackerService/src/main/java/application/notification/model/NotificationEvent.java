package application.notification.model;

import java.util.UUID;

public record NotificationEvent(UUID eventId, String payloadJson) {
}
