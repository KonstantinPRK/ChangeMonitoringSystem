package application.outbox;

import application.config.WorkerProperties;
import application.messenger.MessageDeliveryException;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Определяет решение после ошибки через {@code OutgoingMessageFailurePolicy}.
 */
@Component
public class OutgoingMessageFailurePolicy {
    private static final Duration MAXIMUM_RETRY_DELAY = Duration.ofMinutes(2);
    private final WorkerProperties workerProperties;


    public OutgoingMessageFailurePolicy(WorkerProperties workerProperties) {
        this.workerProperties = workerProperties;
    }


    public boolean isPermanent(Throwable failure) {
        return failure instanceof MessageDeliveryException deliveryFailure
                && deliveryFailure.permanent();
    }


    public Duration retryDelay(StoredOutgoingMessage message, Throwable failure) {
        Duration backoff = exponentialBackoff(message.attempts());

        if (!(failure instanceof MessageDeliveryException deliveryFailure)) {
            return backoff;
        }

        return deliveryFailure.retryAfter().compareTo(backoff) > 0
                ? deliveryFailure.retryAfter()
                : backoff;
    }


    private Duration exponentialBackoff(int attempts) {
        long multiplier = 1L << Math.min(attempts, 6);
        Duration delay = workerProperties.retryDelay().multipliedBy(multiplier);

        return delay.compareTo(MAXIMUM_RETRY_DELAY) > 0
                ? MAXIMUM_RETRY_DELAY
                : delay;
    }
}
