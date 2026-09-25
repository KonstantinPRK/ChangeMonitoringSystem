package application.connector.stackoverflow.quota;

import java.time.Instant;

public record StackOverflowQuota(long remaining, Instant blockedUntil) {
}
