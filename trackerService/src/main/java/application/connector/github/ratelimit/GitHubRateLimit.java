package application.connector.github.ratelimit;

import java.time.Instant;

/**
 * Передаёт между компонентами данные {@code GitHubRateLimit}.
 */
public record GitHubRateLimit(long remaining, Instant resetAt) {
}
