package application;


public class SubscriptionServiceApiException
        extends RuntimeException {

    private final Integer statusCode;


    public SubscriptionServiceApiException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
        statusCode = null;
    }


    public SubscriptionServiceApiException(
            int statusCode,
            String message
    ) {
        super(message);
        this.statusCode = statusCode;
    }


    public SubscriptionServiceApiException(
            int statusCode,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.statusCode = statusCode;
    }


    public Integer statusCode() {
        return statusCode;
    }
}
