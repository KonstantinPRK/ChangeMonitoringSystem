package application.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Публикует данные во внешний транспорт через {@code KafkaPublisher}.
 */
@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Duration publishTimeout;


    public KafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.publish-timeout:10s}") Duration publishTimeout
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.publishTimeout = publishTimeout;
    }


    public void publish(String topic, String key, String payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka publication was interrupted", exception);

        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Kafka did not confirm publication to " + topic, exception);

        }
    }
}
