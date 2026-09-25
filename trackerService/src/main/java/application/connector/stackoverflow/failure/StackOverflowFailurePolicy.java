package application.connector.stackoverflow.failure;

import application.catalog.model.TrackedResource;
import application.connector.ProviderFailurePolicy;
import application.connector.stackoverflow.api.StackOverflowApiException;
import application.tracking.RetryBackoff;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class StackOverflowFailurePolicy implements ProviderFailurePolicy {
    private final RetryBackoff retryBackoff;
    private final Clock clock;


    public StackOverflowFailurePolicy(RetryBackoff retryBackoff, Clock clock) {
        this.retryBackoff = retryBackoff;
        this.clock = clock;
    }


    @Override
    public Instant nextAttempt(TrackedResource resource, Throwable failure) {
        if (!(failure instanceof StackOverflowApiException apiFailure)) {
            return retryBackoff.nextAttempt(resource.failureCount());
        }

        Instant retryAt = apiFailure.retryAt();
        if (retryAt != null && retryAt.isAfter(clock.instant())) return retryAt;
        if (apiFailure.statusCode() == 400 || apiFailure.statusCode() == 404) {
            return clock.instant().plus(Duration.ofHours(1));
        }
        if (apiFailure.statusCode() == 401 || apiFailure.statusCode() == 403) {
            return clock.instant().plus(Duration.ofMinutes(10));
        }
        return retryBackoff.nextAttempt(resource.failureCount());
    }
}
