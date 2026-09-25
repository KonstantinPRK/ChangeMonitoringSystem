package application.subscription.operation;

import java.time.Duration;

public record OperationFailureDecision(boolean retry, Duration retryAfter) {
}
