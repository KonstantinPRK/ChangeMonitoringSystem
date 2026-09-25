package application.user;

import java.time.Instant;
import java.util.UUID;

public record BotUser(UUID id, UserKey key, String messenger, UserStatus status, Instant createdAt) {
}
