package application.connector.github.link;

import application.connector.UnsupportedResourceLinkException;

public class UnsupportedGitHubLinkException extends UnsupportedResourceLinkException {
    public UnsupportedGitHubLinkException(String message) {
        super(message);
    }
}
