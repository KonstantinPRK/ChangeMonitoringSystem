package application.connector.github.failure;

import application.catalog.model.TrackedResource;
import application.connector.ProviderFailurePolicy;
import application.connector.github.api.GitHubApiException;
import application.tracking.RetryBackoff;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Определяет решение после ошибки через {@code GitHubFailurePolicy}.
 */
@Component
public class GitHubFailurePolicy implements ProviderFailurePolicy {
    private final RetryBackoff retryBackoff;
    private final Clock clock;


    public GitHubFailurePolicy(RetryBackoff retryBackoff, Clock clock) {
        this.retryBackoff = retryBackoff;
        this.clock = clock;
    }


    @Override
    public Instant nextAttempt(TrackedResource resource, Throwable failure) {
        if (!(failure instanceof GitHubApiException apiFailure)) {
            return retryBackoff.nextAttempt(resource.failureCount());
        }

        Instant retryAt = apiFailure.retryAt();
        if (retryAt != null && retryAt.isAfter(clock.instant())) return retryAt;
        if (apiFailure.statusCode() == 404 || apiFailure.statusCode() == 410) {
            return clock.instant().plus(Duration.ofHours(1));
        }
        if (apiFailure.statusCode() == 401 || apiFailure.statusCode() == 403) {
            return clock.instant().plus(Duration.ofMinutes(10));
        }
        return retryBackoff.nextAttempt(resource.failureCount());
    }
}
