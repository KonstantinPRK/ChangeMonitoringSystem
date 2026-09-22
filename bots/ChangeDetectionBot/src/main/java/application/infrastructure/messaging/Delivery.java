package application.infrastructure.messaging;

public record Delivery<T>(
    T message,
    Runnable acknowledgeAction,
    Runnable retryAction
) {
    public void acknowledge() {
        acknowledgeAction.run();
    }

    public void retry() {
        retryAction.run();
    }
}

