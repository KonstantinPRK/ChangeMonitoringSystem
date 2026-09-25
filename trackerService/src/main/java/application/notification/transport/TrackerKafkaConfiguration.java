package application.notification.transport;

import application.config.KafkaProperties;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class TrackerKafkaConfiguration {
    @Bean
    public NewTopic updateTopic(KafkaProperties kafkaProperties) {
        return TopicBuilder.name(kafkaProperties.updateTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
