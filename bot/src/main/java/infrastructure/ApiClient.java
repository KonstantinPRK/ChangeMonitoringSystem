package infrastructure;

import infrastructure.telegram.TelegramMessage;

import java.util.List;

public interface ApiClient {
    List<String> getUpdates();

    void sendMessage(String chatId, String text);
}
