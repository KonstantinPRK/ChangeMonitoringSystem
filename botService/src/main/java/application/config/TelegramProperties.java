package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.telegram")
public record TelegramProperties(
        boolean enabled,
        String botId,
        String token,
        Duration longPollTimeout,
        Duration retryDelay
) {
}
