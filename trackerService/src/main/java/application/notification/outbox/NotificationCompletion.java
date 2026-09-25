package application.notification.outbox;

import application.metrics.NotificationMetrics;
import application.notification.model.StoredNotification;
import application.notification.transport.PublicationResult;
import application.notification.transport.PublicationStatus;

import org.springframework.stereotype.Component;

@Component
public class NotificationCompletion {
    private final NotificationOutboxRepository outboxRepository;
    private final NotificationFailurePolicy failurePolicy;
    private final NotificationMetrics notificationMetrics;


    public NotificationCompletion(
            NotificationOutboxRepository outboxRepository,
            NotificationFailurePolicy failurePolicy,
            NotificationMetrics notificationMetrics
    ) {
        this.outboxRepository = outboxRepository;
        this.failurePolicy = failurePolicy;
        this.notificationMetrics = notificationMetrics;
    }


    public void complete(StoredNotification notification, PublicationResult result) {
        if (result.status() == PublicationStatus.COMPLETED) {
            outboxRepository.complete(notification);
            notificationMetrics.record("completed");
            return;
        }
        if (result.status() == PublicationStatus.RETRY) {
            outboxRepository.retry(notification, result.retryAfter(), result.reason());
            notificationMetrics.record("retry");
            return;
        }

        outboxRepository.fail(notification, result.reason());
        notificationMetrics.record("failed");
    }


    public void fail(StoredNotification notification, Throwable failure) {
        if (failurePolicy.permanent(notification, failure)) {
            outboxRepository.fail(notification, failure.getMessage());
            notificationMetrics.record("failed");
            return;
        }

        outboxRepository.retry(
                notification,
                failurePolicy.retryDelay(notification),
                failure.getMessage()
        );
        notificationMetrics.record("retry");
    }
}
