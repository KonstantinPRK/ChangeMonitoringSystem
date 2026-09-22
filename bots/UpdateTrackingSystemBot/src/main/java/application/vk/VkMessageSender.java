package application.vk;

import application.messenger.CommunicationChannel;
import application.messenger.MessageSender;
import application.messenger.SubscriberId;

public final class VkMessageSender implements MessageSender {

    private final VkApiClient apiClient;

    public VkMessageSender(VkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void send(SubscriberId subscriberId, String text) {
        if (subscriberId.communicationChannel() != CommunicationChannel.VK) {
            throw new IllegalArgumentException("VK sender requires a VK subscriber");
        }

        apiClient.sendMessage(subscriberId.platformId(), text);
    }
}
