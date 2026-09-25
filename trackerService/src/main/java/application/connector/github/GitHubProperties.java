package application.connector.github;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

@ConfigurationProperties("app.github")
public record GitHubProperties(
        URI apiUrl,
        String token,
        String apiVersion,
        Duration requestTimeout,
        Duration scrapeInterval,
        Set<String> supportedHosts
) {
}
