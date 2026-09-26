package application.connector.github.api;

import java.time.Instant;

/**
 * Сигнализирует об ошибке, представленной типом {@code GitHubApiException}.
 */
public class GitHubApiException extends RuntimeException {
    private final int statusCode;
    private final Instant retryAt;


    public GitHubApiException(int statusCode, String message, Instant retryAt) {
        super(message);
        this.statusCode = statusCode;
        this.retryAt = retryAt;
    }


    public int statusCode() {
        return statusCode;
    }


    public Instant retryAt() {
        return retryAt;
    }
}
