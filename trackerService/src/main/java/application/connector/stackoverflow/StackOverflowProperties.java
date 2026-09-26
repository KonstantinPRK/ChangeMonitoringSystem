package application.connector.stackoverflow;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

/**
 * Передаёт между компонентами данные {@code StackOverflowProperties}.
 */
@ConfigurationProperties("app.stack-overflow")
public record StackOverflowProperties(
        URI apiUrl,
        String site,
        String key,
        Duration requestTimeout,
        Duration scrapeInterval,
        Set<String> supportedHosts
) {
}
