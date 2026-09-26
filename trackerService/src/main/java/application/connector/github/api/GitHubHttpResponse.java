package application.connector.github.api;

import java.time.Duration;
import java.time.Instant;

/**
 * Передаёт между компонентами данные {@code GitHubHttpResponse}.
 */
public record GitHubHttpResponse(
        int statusCode,
        String body,
        String etag,
        Long rateLimitRemaining,
        Instant rateLimitReset,
        Duration retryAfter
) {
}
