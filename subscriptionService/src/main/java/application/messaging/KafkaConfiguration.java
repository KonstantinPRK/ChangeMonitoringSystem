package application.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaConfiguration {
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            InvalidUpdateRecoverer recoverer,
            @Value("${app.kafka.retry-interval:2s}") Duration retryInterval
    ) {
        FixedBackOff backOff = new FixedBackOff(retryInterval.toMillis(), FixedBackOff.UNLIMITED_ATTEMPTS);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.setClassifications(Map.of(InvalidUpdateException.class, false), true);
        return errorHandler;
    }


    @Bean
    public NewTopic updateTopic(
            @Value("${app.kafka.update-topic}") String topic,
            @Value("${app.kafka.partitions:3}") int partitions,
            @Value("${app.kafka.replicas:1}") int replicas
    ) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicas).build();
    }


    @Bean
    public NewTopic notificationTopic(
            @Value("${app.kafka.notification-topic}") String topic,
            @Value("${app.kafka.partitions:3}") int partitions,
            @Value("${app.kafka.replicas:1}") int replicas
    ) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicas).build();
    }


    @Bean
    public NewTopic deadLetterTopic(
            @Value("${app.kafka.dlq-topic}") String topic,
            @Value("${app.kafka.partitions:3}") int partitions,
            @Value("${app.kafka.replicas:1}") int replicas
    ) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicas).build();
    }
}
