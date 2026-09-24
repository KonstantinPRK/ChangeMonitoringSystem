package application;

import application.commandHandler.CommandType;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

public class MessengerDispatcher {
    TelegramClient telegramClient;
    Executor executor;

    private BlockingQueue<TelegramMessage> messageInputQueue = new ArrayBlockingQueue<>(100);
    private BlockingQueue<TelegramMessage> messageOutputQueue = new ArrayBlockingQueue<>(100);

    public void start(){
        executor.execute(this::processIncomingMessages);
        executor.execute(this::processOutgoingMessages);
    }

    public void stop(){

    }

    private void processOutgoingMessages() {
        try {
            while (true) {
                TelegramMessage message = messageOutputQueue.take();
                telegramClient.sendMessage(message);

            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


    private void processIncomingMessages() {
        try {
            while (true) {
                TelegramMessage message = messageInputQueue.take();

            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }

    public void sendMessage(String message) {
        
    }

    public CommandType getCommand(User user) {
        return null;
    }

    public String getMessage(User user) {
        return null;
    }

    public void sendMessage() {
    }
}
