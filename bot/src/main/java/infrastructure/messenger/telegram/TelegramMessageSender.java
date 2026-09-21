package infrastructure.messenger.telegram;

import application.messenger.MessageSender;
import application.messenger.MessengerType;
import application.messenger.SubscriberId;

public final class TelegramMessageSender implements MessageSender {

    private final TelegramApiClient client;

    public TelegramMessageSender(TelegramApiClient client) {
        this.client = client;
    }

    @Override
    public void send(SubscriberId subscriberId, String text) {
        if (subscriberId.messengerType() != MessengerType.TELEGRAM) {
            throw new IllegalArgumentException("Telegram sender requires a Telegram subscriber");
        }

        client.sendMessage(subscriberId.platformId(), text);
    }
}
