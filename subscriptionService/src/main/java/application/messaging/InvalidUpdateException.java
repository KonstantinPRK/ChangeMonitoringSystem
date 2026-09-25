package application.messaging;

public class InvalidUpdateException extends RuntimeException {
    public InvalidUpdateException(String message) {
        super(message);
    }

    public InvalidUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
