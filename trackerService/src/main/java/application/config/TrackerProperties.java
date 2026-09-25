package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("app.tracker")
public record TrackerProperties(
        String id,
        URI baseUrl,
        String internalApiToken,
        Duration availabilityInterval
) {
}
