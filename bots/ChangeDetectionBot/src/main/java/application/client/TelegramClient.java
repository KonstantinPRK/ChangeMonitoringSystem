package application.client;


import application.commandHandler.CommandHandler;
import application.commandHandler.CommandType;
import application.listener.TelegramMessageListener;
import application.sender.TelegramMessageSender;

import java.util.Map;


public final class TelegramClient {
    TelegramMessageListener listener;
    TelegramMessageSender sender;
    Map<CommandType, CommandHandler> handlerMap;


}