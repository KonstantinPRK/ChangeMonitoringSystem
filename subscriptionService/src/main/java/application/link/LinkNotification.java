package application.link;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record LinkNotification(
        @NotNull UUID eventId,
        @NotBlank @Size(max = 128) String trackerId,
        @NotNull @Valid Link link,
        @NotBlank @Size(max = 10000) String message,
        @NotNull Instant occurredAt
) {
}
