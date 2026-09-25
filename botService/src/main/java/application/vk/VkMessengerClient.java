package application.vk;

import application.config.VkProperties;
import application.messenger.MessageSender;
import application.messenger.MessageUpdateSource;
import application.messenger.MessengerClient;
import application.messenger.MessengerType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.vk.enabled", havingValue = "true")
public class VkMessengerClient implements MessengerClient {
    private final VkProperties vkProperties;
    private final VkUpdateSource updateSource;
    private final VkMessageSender messageSender;


    public VkMessengerClient(
            VkProperties vkProperties,
            VkUpdateSource updateSource,
            VkMessageSender messageSender
    ) {
        this.vkProperties = vkProperties;
        this.updateSource = updateSource;
        this.messageSender = messageSender;
    }


    @Override
    public String botId() {
        return vkProperties.botId();
    }


    @Override
    public MessengerType messengerType() {
        return MessengerType.VK;
    }


    @Override
    public MessageUpdateSource updateSource() {
        return updateSource;
    }


    @Override
    public MessageSender messageSender() {
        return messageSender;
    }
}
