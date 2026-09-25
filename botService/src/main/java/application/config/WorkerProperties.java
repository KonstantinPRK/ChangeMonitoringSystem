package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.workers")
public record WorkerProperties(Duration recoveryInterval, Duration retryDelay, int maximumAttempts) {
}
