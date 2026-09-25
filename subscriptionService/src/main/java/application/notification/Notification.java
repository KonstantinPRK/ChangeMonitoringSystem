package application.notification;

import application.link.Link;
import application.user.User;

import java.time.Instant;
import java.util.UUID;

public record Notification(UUID notificationId, UUID eventId, User user, Link link, String message, Instant occurredAt) {
}
