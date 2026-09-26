package application.connector.stackoverflow.api;

import java.time.Instant;

/**
 * Сигнализирует об ошибке, представленной типом {@code StackOverflowApiException}.
 */
public class StackOverflowApiException extends RuntimeException {
    private final int statusCode;
    private final Instant retryAt;


    public StackOverflowApiException(int statusCode, String message, Instant retryAt) {
        super(message);
        this.statusCode = statusCode;
        this.retryAt = retryAt;
    }


    public int statusCode() {
        return statusCode;
    }


    public Instant retryAt() {
        return retryAt;
    }
}
