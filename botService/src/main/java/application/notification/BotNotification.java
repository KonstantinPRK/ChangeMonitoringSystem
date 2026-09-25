package application.notification;

import application.subscription.model.Link;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record BotNotification(
        @NotNull UUID notificationId,
        @NotNull UUID eventId,
        @Valid @NotNull NotificationUser user,
        @Valid @NotNull Link link,
        @NotBlank String message,
        @NotNull Instant occurredAt
) {
}
