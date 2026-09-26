package application.catalog.api;

/**
 * Сигнализирует об ошибке, представленной типом {@code SubscriptionServiceException}.
 */
public class SubscriptionServiceException extends RuntimeException {
    private final int statusCode;


    public SubscriptionServiceException(int statusCode, String responseBody) {
        super("SubscriptionService returned HTTP " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
    }


    public int statusCode() {
        return statusCode;
    }
}
