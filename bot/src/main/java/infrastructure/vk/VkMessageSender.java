package infrastructure.vk;

import application.messenger.MessageSender;
import infrastructure.MessengerType;
import application.messenger.SubscriberId;

public final class VkMessageSender implements MessageSender {

    private final VkApiClient apiClient;

    public VkMessageSender(VkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void send(SubscriberId subscriberId, String text) {
        if (subscriberId.messengerType() != MessengerType.VK) {
            throw new IllegalArgumentException("VK sender requires a VK subscriber");
        }

        apiClient.sendMessage(subscriberId.platformId(), text);
    }
}
