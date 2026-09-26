package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Передаёт между компонентами данные {@code TelegramProperties}.
 */
@ConfigurationProperties("app.telegram")
public record TelegramProperties(
        boolean enabled,
        String botId,
        String token,
        Duration longPollTimeout,
        Duration retryDelay
) {
}
