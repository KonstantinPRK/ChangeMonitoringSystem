package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.kafka")
public record KafkaProperties(String updateTopic) {
}
