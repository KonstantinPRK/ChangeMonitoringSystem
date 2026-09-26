package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Передаёт между компонентами данные {@code SubscriptionProperties}.
 */
@ConfigurationProperties("app.subscription")
public record SubscriptionProperties(
        URI baseUrl,
        Duration heartbeatInterval,
        Duration requestRetryDelay,
        long cacheTtlSeconds,
        Kafka kafka
) {
    public record Kafka(String topic, String deadLetterTopic, String groupId) {
    }
}
