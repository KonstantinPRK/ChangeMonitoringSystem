package application.notification;

import application.config.SubscriptionProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class NotificationDeadLetterPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SubscriptionProperties subscriptionProperties;


    public NotificationDeadLetterPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            SubscriptionProperties subscriptionProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.subscriptionProperties = subscriptionProperties;
    }


    public void publish(String json) {
        try {
            kafkaTemplate.send(subscriptionProperties.kafka().deadLetterTopic(), json)
                    .get(10, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DLQ publication was interrupted", exception);

        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Notification cannot be published to the DLQ", exception);

        }
    }
}
