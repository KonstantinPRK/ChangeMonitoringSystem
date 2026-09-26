package application.connector.github.api;

import application.snapshot.ResourceObservation;
import application.snapshot.ResourceSnapshot;
import application.connector.github.ratelimit.GitHubRateLimit;
import application.connector.github.ratelimit.GitHubRateLimitCache;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

/**
 * Читает и преобразует входные данные для {@code GitHubObservationReader}.
 */
@Component
public class GitHubObservationReader {
    private final ObjectMapper objectMapper;
    private final GitHubRateLimitCache rateLimitCache;
    private final Clock clock;


    public GitHubObservationReader(
            ObjectMapper objectMapper,
            GitHubRateLimitCache rateLimitCache,
            Clock clock
    ) {
        this.objectMapper = objectMapper;
        this.rateLimitCache = rateLimitCache;
        this.clock = clock;
    }


    public ResourceObservation read(
            GitHubHttpResponse response,
            Function<JsonNode, ResourceSnapshot> snapshotReader
    ) {
        saveRateLimit(response);
        if (response.statusCode() == 304) return ResourceObservation.unchanged(response.etag());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw failure(response);

        JsonNode body = objectMapper.readTree(response.body());
        return ResourceObservation.changed(response.etag(), snapshotReader.apply(body));
    }


    private void saveRateLimit(GitHubHttpResponse response) {
        if (response.rateLimitRemaining() == null || response.rateLimitReset() == null) return;
        rateLimitCache.update(new GitHubRateLimit(
                response.rateLimitRemaining(),
                response.rateLimitReset()
        ));
    }


    private GitHubApiException failure(GitHubHttpResponse response) {
        Instant retryAt = response.rateLimitReset();
        if (response.retryAfter() != null) retryAt = clock.instant().plus(response.retryAfter());
        return new GitHubApiException(response.statusCode(), response.body(), retryAt);
    }
}
