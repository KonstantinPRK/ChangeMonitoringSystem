package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Передаёт между компонентами данные {@code SubscriptionServiceProperties}.
 */
@ConfigurationProperties("app.subscription-service")
public record SubscriptionServiceProperties(
        URI baseUrl,
        Duration synchronizationInterval,
        Duration synchronizationLease,
        Duration requestRetryDelay
) {
}
