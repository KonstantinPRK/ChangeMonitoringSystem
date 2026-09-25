package application.notification.transport;

import application.config.KafkaProperties;
import application.notification.model.StoredNotification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaLinkNotificationPublisher implements LinkNotificationPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;


    public KafkaLinkNotificationPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaProperties kafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }


    @Override
    public CompletionStage<PublicationResult> publish(StoredNotification notification) {
        String key = notification.eventId().toString();
        return kafkaTemplate.send(kafkaProperties.updateTopic(), key, notification.payloadJson())
                .thenApply(result -> PublicationResult.completed());
    }
}
