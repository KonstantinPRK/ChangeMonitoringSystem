package application.tracking;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Реализует ответственность компонента {@code RetryBackoff}.
 */
@Component
public class RetryBackoff {
    private final Clock clock;


    public RetryBackoff(Clock clock) {
        this.clock = clock;
    }


    public Instant nextAttempt(int failureCount) {
        int exponent = Math.min(failureCount + 1, 9);
        Duration delay = Duration.ofSeconds(1L << exponent);
        return clock.instant().plus(delay);
    }
}
