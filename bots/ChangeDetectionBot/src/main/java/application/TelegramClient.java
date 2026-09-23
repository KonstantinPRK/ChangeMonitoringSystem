package application;


import java.util.Map;


public final class TelegramClient {
    TelegramApiClient telegramApiClient;
    Map<CommandType, CommandHandler> handlerMap;


    public CommandType getCommand(User user) {
        return null;
    }


    public void sendMessage(TelegramMessage telegramMessage) {
    }
}