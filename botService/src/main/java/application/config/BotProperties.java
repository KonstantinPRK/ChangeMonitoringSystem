package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

/**
 * Передаёт между компонентами данные {@code BotProperties}.
 */
@ConfigurationProperties("app.bot")
public record BotProperties(URI baseUrl, String internalApiToken) {
}
