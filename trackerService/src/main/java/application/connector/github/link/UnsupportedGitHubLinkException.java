package application.connector.github.link;

import application.connector.UnsupportedResourceLinkException;

/**
 * Сигнализирует об ошибке, представленной типом {@code UnsupportedGitHubLinkException}.
 */
public class UnsupportedGitHubLinkException extends UnsupportedResourceLinkException {
    public UnsupportedGitHubLinkException(String message) {
        super(message);
    }
}
