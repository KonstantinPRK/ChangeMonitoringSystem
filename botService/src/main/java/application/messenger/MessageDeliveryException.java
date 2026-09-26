package application.messenger;

import java.time.Duration;

/**
 * Сигнализирует об ошибке, представленной типом {@code MessageDeliveryException}.
 */
public class MessageDeliveryException extends RuntimeException {
    private final boolean permanent;
    private final Duration retryAfter;


    public MessageDeliveryException(String message, boolean permanent, Duration retryAfter) {
        super(message);
        this.permanent = permanent;
        this.retryAfter = retryAfter;
    }


    public boolean permanent() {
        return permanent;
    }


    public Duration retryAfter() {
        return retryAfter;
    }
}
