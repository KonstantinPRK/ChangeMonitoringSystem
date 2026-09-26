package application.connector;

/**
 * Сигнализирует об ошибке, представленной типом {@code UnsupportedResourceLinkException}.
 */
public class UnsupportedResourceLinkException extends RuntimeException {
    public UnsupportedResourceLinkException(String message) {
        super(message);
    }
}
