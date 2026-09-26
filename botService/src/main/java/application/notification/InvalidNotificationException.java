package application.notification;

/**
 * Сигнализирует об ошибке, представленной типом {@code InvalidNotificationException}.
 */
public class InvalidNotificationException extends RuntimeException {
    public InvalidNotificationException(String message) {
        super(message);
    }


    public InvalidNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
