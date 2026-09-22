package application.domain;

import java.time.Instant;
import java.util.UUID;

public record BotUser(UUID id, String platformUserId, Instant createdAt) {
}

