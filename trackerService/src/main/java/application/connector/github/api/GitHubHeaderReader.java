package application.connector.github.api;

import org.springframework.stereotype.Component;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Читает и преобразует входные данные для {@code GitHubHeaderReader}.
 */
@Component
public class GitHubHeaderReader {
    public GitHubHttpResponse read(HttpResponse<String> response) {
        HttpHeaders headers = response.headers();
        return new GitHubHttpResponse(
                response.statusCode(),
                response.body(),
                headers.firstValue("ETag").orElse(null),
                longHeader(headers, "X-RateLimit-Remaining"),
                instantHeader(headers, "X-RateLimit-Reset"),
                durationHeader(headers, "Retry-After")
        );
    }


    private Long longHeader(HttpHeaders headers, String name) {
        return headers.firstValue(name).map(Long::valueOf).orElse(null);
    }


    private Instant instantHeader(HttpHeaders headers, String name) {
        Long epochSeconds = longHeader(headers, name);
        return epochSeconds == null ? null : Instant.ofEpochSecond(epochSeconds);
    }


    private Duration durationHeader(HttpHeaders headers, String name) {
        Long seconds = longHeader(headers, name);
        return seconds == null ? null : Duration.ofSeconds(seconds);
    }
}
