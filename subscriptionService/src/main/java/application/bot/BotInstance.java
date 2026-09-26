package application.bot;

import java.net.URI;
import java.time.Instant;

/**
 * Передаёт между компонентами данные {@code BotInstance}.
 */
public record BotInstance(String botId, URI baseUrl, Instant availableUntil) {
}
