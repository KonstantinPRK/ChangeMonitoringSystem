package application.user;

import java.time.Instant;
import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code BotUser}.
 */
public record BotUser(UUID id, UserKey key, String messenger, UserStatus status, Instant createdAt) {
}
