package application.notification;

import application.messaging.KafkaPublisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Отправляет данные во внешний транспорт через {@code KafkaNotificationSender}.
 */
@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaNotificationSender implements NotificationSender {
    private final KafkaPublisher publisher;
    private final ObjectMapper objectMapper;
    private final String notificationTopic;


    public KafkaNotificationSender(
            KafkaPublisher publisher,
            ObjectMapper objectMapper,
            @Value("${app.kafka.notification-topic}") String notificationTopic
    ) {
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.notificationTopic = notificationTopic;
    }


    @Override
    public void send(Notification notification) {
        String payload = objectMapper.writeValueAsString(notification);
        publisher.publish(notificationTopic, notification.user().botId(), payload);
    }
}
