package application.bot;

import java.net.URI;
import java.time.Instant;

public record BotInstance(String botId, URI baseUrl, Instant availableUntil) {
}
