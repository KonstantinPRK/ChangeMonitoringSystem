package application.domain;

import java.time.Instant;
import java.util.UUID;

public record Subscription(
    UUID id,
    UUID linkId,
    RecipientAddress recipient,
    Instant createdAt
) {
}

