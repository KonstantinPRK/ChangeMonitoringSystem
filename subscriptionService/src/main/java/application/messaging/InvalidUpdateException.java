package application.messaging;

/**
 * Сигнализирует об ошибке, представленной типом {@code InvalidUpdateException}.
 */
public class InvalidUpdateException extends RuntimeException {
    public InvalidUpdateException(String message) {
        super(message);
    }

    public InvalidUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
