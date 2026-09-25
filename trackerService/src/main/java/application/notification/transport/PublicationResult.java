package application.notification.transport;

import java.time.Duration;

public record PublicationResult(PublicationStatus status, Duration retryAfter, String reason) {
    public static PublicationResult completed() {
        return new PublicationResult(PublicationStatus.COMPLETED, Duration.ZERO, "");
    }


    public static PublicationResult retry(Duration delay, String reason) {
        return new PublicationResult(PublicationStatus.RETRY, delay, reason);
    }


    public static PublicationResult failed(String reason) {
        return new PublicationResult(PublicationStatus.FAILED, Duration.ZERO, reason);
    }
}
