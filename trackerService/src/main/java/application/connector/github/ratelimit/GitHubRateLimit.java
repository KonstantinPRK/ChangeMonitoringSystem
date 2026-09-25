package application.connector.github.ratelimit;

import java.time.Instant;

public record GitHubRateLimit(long remaining, Instant resetAt) {
}
