package application.infrastructure.telegram;

import application.infrastructure.messaging.MessageSender;
import java.util.concurrent.CompletableFuture;

public final class TelegramMessageSender implements MessageSender<TelegramMessage> {

    private final TelegramApiClient client;

    public TelegramMessageSender(TelegramApiClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<Void> send(TelegramMessage message) {
        return client.sendMessage(message.chatId(), message.text());
    }
}
