package application.telegram;

import application.config.TelegramProperties;
import application.messenger.MessageSender;
import application.messenger.MessageUpdateSource;
import application.messenger.MessengerClient;
import application.messenger.MessengerType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramMessengerClient implements MessengerClient {
    private final TelegramProperties telegramProperties;
    private final TelegramUpdateSource updateSource;
    private final TelegramMessageSender messageSender;


    public TelegramMessengerClient(
            TelegramProperties telegramProperties,
            TelegramUpdateSource updateSource,
            TelegramMessageSender messageSender
    ) {
        this.telegramProperties = telegramProperties;
        this.updateSource = updateSource;
        this.messageSender = messageSender;
    }


    @Override
    public String botId() {
        return telegramProperties.botId();
    }


    @Override
    public MessengerType messengerType() {
        return MessengerType.TELEGRAM;
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
