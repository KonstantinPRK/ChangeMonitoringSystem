package application.messaging;

import application.metrics.ServiceMetrics;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.stereotype.Component;

/**
 * Реализует ответственность компонента {@code InvalidUpdateRecoverer}.
 */
@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class InvalidUpdateRecoverer implements ConsumerRecordRecoverer {
    private final KafkaPublisher publisher;
    private final ServiceMetrics metrics;
    private final String deadLetterTopic;


    public InvalidUpdateRecoverer(
            KafkaPublisher publisher,
            ServiceMetrics metrics,
            @Value("${app.kafka.dlq-topic}") String deadLetterTopic
    ) {
        this.publisher = publisher;
        this.metrics = metrics;
        this.deadLetterTopic = deadLetterTopic;
    }


    @Override
    public void accept(ConsumerRecord<?, ?> record, Exception exception) {
        if (!isInvalidUpdate(exception)) throw new IllegalStateException("Update must be retried", exception);
        String key = record.key() == null ? null : record.key().toString();
        String payload = record.value() == null ? null : record.value().toString();
        publisher.publish(deadLetterTopic, key, payload);
        metrics.delivered("dlq");
    }


    private boolean isInvalidUpdate(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof InvalidUpdateException) return true;
            cause = cause.getCause();
        }
        return false;
    }
}
