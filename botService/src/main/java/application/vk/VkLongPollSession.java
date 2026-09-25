package application.vk;

import java.net.URI;

public record VkLongPollSession(URI server, String key, long timestamp) {
}
