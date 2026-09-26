package application.subscription.operation;

import java.time.Duration;

/**
 * Передаёт между компонентами данные {@code OperationFailureDecision}.
 */
public record OperationFailureDecision(boolean retry, Duration retryAfter) {
}
