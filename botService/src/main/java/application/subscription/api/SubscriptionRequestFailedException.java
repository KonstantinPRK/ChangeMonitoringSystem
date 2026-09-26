package application.subscription.api;

/**
 * Сигнализирует об ошибке, представленной типом {@code SubscriptionRequestFailedException}.
 */
public class SubscriptionRequestFailedException extends RuntimeException {
    public SubscriptionRequestFailedException(String message) {
        super(message);
    }
}
