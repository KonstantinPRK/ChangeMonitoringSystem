package application.connector.github.api;

import java.time.Duration;
import java.time.Instant;

public record GitHubHttpResponse(
        int statusCode,
        String body,
        String etag,
        Long rateLimitRemaining,
        Instant rateLimitReset,
        Duration retryAfter
) {
}
