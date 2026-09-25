package application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.vk")
public record VkProperties(
        boolean enabled,
        String botId,
        String token,
        long groupId,
        String apiVersion,
        Duration longPollWait,
        Duration retryDelay
) {
}
