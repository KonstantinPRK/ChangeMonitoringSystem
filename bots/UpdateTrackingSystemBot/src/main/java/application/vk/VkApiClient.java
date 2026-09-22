package application.vk;

import java.util.List;

public interface VkApiClient {
    List<VkMessage> getUpdates();

    void sendMessage(String peerId, String text);
}
