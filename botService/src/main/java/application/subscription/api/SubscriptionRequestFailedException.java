package application.subscription.api;

public class SubscriptionRequestFailedException extends RuntimeException {
    public SubscriptionRequestFailedException(String message) {
        super(message);
    }
}
