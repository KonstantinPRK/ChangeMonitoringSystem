package application.registration;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Определяет решение после ошибки через {@code RegistrationAvailabilityPolicy}.
 */
public class RegistrationAvailabilityPolicy {
    private final Clock clock;
    private final Duration availabilityTimeout;


    public RegistrationAvailabilityPolicy(
        Clock clock,
        Duration availabilityTimeout
    ) {
        this.clock = clock;
        this.availabilityTimeout = availabilityTimeout;

        if (availabilityTimeout.isNegative() || availabilityTimeout.isZero()) {
            throw new IllegalArgumentException(
                "Registration availability timeout must be positive"
            );
        }
    }


    public Instant nextDeadline() {
        return clock.instant().plus(availabilityTimeout);
    }


    public void requireAvailable(
        Instant availableUntil,
        String serviceName
    ) {
        if (!clock.instant().isBefore(availableUntil)) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                serviceName + " has not confirmed availability"
            );
        }
    }
}
