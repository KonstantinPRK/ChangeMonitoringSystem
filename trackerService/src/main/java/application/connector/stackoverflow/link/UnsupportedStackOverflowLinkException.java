package application.connector.stackoverflow.link;

import application.connector.UnsupportedResourceLinkException;

public class UnsupportedStackOverflowLinkException extends UnsupportedResourceLinkException {
    public UnsupportedStackOverflowLinkException(String message) {
        super(message);
    }
}
