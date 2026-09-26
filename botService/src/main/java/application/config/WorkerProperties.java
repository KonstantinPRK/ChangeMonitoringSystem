package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Передаёт между компонентами данные {@code WorkerProperties}.
 */
@ConfigurationProperties("app.workers")
public record WorkerProperties(Duration recoveryInterval, Duration retryDelay, int maximumAttempts) {
}
