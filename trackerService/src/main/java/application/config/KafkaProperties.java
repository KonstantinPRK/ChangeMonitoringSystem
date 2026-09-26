package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Передаёт между компонентами данные {@code KafkaProperties}.
 */
@ConfigurationProperties("app.kafka")
public record KafkaProperties(String updateTopic) {
}
