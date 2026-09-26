package application.vk;

import java.net.URI;

/**
 * Передаёт между компонентами данные {@code VkLongPollSession}.
 */
public record VkLongPollSession(URI server, String key, long timestamp) {
}
