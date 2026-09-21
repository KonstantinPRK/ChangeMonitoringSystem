package infrastructure.messenger.telegram;

import java.util.List;

/**
 * Transport boundary for private Telegram text messages.
 * Implementations are responsible for update offsets and filtering out
 * non-text updates and messages from non-private chats.
 */
public interface TelegramApiClient {

    List<TelegramMessage> getUpdates();

    void sendMessage(String chatId, String text);
}
