package application.connector.stackoverflow.link;

import application.connector.UnsupportedResourceLinkException;

/**
 * Сигнализирует об ошибке, представленной типом {@code UnsupportedStackOverflowLinkException}.
 */
public class UnsupportedStackOverflowLinkException extends UnsupportedResourceLinkException {
    public UnsupportedStackOverflowLinkException(String message) {
        super(message);
    }
}
