package application.connector.stackoverflow.quota;

import java.time.Instant;

/**
 * Передаёт между компонентами данные {@code StackOverflowQuota}.
 */
public record StackOverflowQuota(long remaining, Instant blockedUntil) {
}
