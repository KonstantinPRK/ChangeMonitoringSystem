package application;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

public class MessengerDispatcher {
    private final BlockingQueue<TelegramMessage> messageInputQueue =
        new ArrayBlockingQueue<>(100);

    private final BlockingQueue<TelegramMessage> messageOutputQueue =
        new ArrayBlockingQueue<>(100);

    TelegramClient telegramClient;
    Executor executor;
    User user;

    private volatile boolean running;
    private volatile Thread outgoingMessageThread;


    public void start() {
        if (running) return;

        running = true;
        executor.execute(this::processOutgoingMessages);
    }


    public void stop() {
        running = false;

        Thread thread = outgoingMessageThread;

        if (thread != null) thread.interrupt();
    }


    public void putMessage(TelegramMessage message)
        throws InterruptedException {

        messageInputQueue.put(message);
    }


    public TelegramMessage takeMessage()
        throws InterruptedException {

        return messageInputQueue.take();
    }


    public void sendMessage(String text) {
        try {
            TelegramMessage message = new TelegramMessage(
                user.senderId(),
                user.chatId(),
                text
            );

            messageOutputQueue.put(message);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


    private void processOutgoingMessages() {
        outgoingMessageThread = Thread.currentThread();

        try {
            while (running) {
                TelegramMessage message =
                    messageOutputQueue.take();

                telegramClient.sendMessage(message);
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        } finally {
            running = false;
            outgoingMessageThread = null;

        }
    }
}
