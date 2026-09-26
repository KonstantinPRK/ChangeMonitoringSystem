package application.notification;

import application.config.SubscriptionProperties;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Создаёт и связывает Spring-компоненты для {@code BotKafkaConfiguration}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class BotKafkaConfiguration {
    @Bean
    public DefaultErrorHandler notificationErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(2_000L, FixedBackOff.UNLIMITED_ATTEMPTS));
    }


    @Bean
    public NewTopic notificationTopic(SubscriptionProperties properties) {
        return TopicBuilder.name(properties.kafka().topic())
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic notificationDeadLetterTopic(SubscriptionProperties properties) {
        return TopicBuilder.name(properties.kafka().deadLetterTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
