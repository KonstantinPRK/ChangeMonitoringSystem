package application.notification.transport;

import application.notification.model.StoredNotification;

import java.util.concurrent.CompletionStage;

public interface LinkNotificationPublisher {
    CompletionStage<PublicationResult> publish(StoredNotification notification);
}
