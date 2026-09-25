package application.subscription.operation;

import java.time.Duration;

public record OperationExecution(boolean completed, Duration retryAfter, String reason) {
    public static OperationExecution success() {
        return new OperationExecution(true, Duration.ZERO, "");
    }


    public static OperationExecution retry(Duration delay, String reason) {
        return new OperationExecution(false, delay, reason);
    }
}
