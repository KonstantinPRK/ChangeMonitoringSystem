package application.notification.transport;

import application.notification.model.StoredNotification;

import java.util.concurrent.CompletionStage;

/**
 * Публикует данные во внешний транспорт через {@code LinkNotificationPublisher}.
 */
public interface LinkNotificationPublisher {
    /**
     * Публикует одно долговечное уведомление об изменении.
     *
     * @param notification захваченная запись уведомления
     * @return асинхронный результат публикации
     */
    CompletionStage<PublicationResult> publish(StoredNotification notification);
}
