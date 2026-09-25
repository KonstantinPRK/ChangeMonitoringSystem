package application.notification.model;

import application.catalog.model.Link;

import java.time.Instant;
import java.util.UUID;

public record LinkNotification(
        UUID eventId,
        String trackerId,
        Link link,
        String message,
        Instant occurredAt
) {
}
