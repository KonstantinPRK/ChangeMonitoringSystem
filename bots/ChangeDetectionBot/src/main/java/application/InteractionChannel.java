package application;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;


public final class InteractionChannel {

    private static TelegramClient telegramClient;
    private static SubscriptionQueue subscriptionQueue;
    private static Executor executor;

    private final User user;

    private final BlockingQueue<TelegramMessage> messageInputQueue = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<TelegramMessage> messageOutputQueue = new ArrayBlockingQueue<>(100);

    private volatile boolean channelIsOpen;


    private InteractionChannel(User user) {
        this.user = user;
    }


    public static InteractionChannel openNewInteractionChannel(User user) {
        InteractionChannel channel = new InteractionChannel(user);

        channel.working();

        return channel;
    }


    public void putIncomingMessage(TelegramMessage message)
            throws InterruptedException {

        messageInputQueue.put(message);
    }


    public TelegramMessage takeIncomingMessage()
            throws InterruptedException {

        return messageInputQueue.take();
    }


    public void sendMessage(String text)
            throws InterruptedException {

        TelegramMessage message = new TelegramMessage(
                user.senderId(),
                user.chatId(),
                text
        );

        messageOutputQueue.put(message);
    }


    public void putNotification(Notification notification)
            throws InterruptedException {

        sendMessage(notification.message());
    }


    public void publishSubscription(Subscription subscription)
            throws InterruptedException {

        subscriptionQueue.publishSubscription(subscription);
    }


    private void working() {
        channelIsOpen = true;

        executor.execute(this::processIncomingMessages);
        executor.execute(this::sendOutgoingMessages);
    }


    private void processIncomingMessages() {
        try {
            while (channelIsOpen) {
                TelegramMessage message = messageInputQueue.take();

                parseCommand(message.text());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


    private void sendOutgoingMessages() {
        try {
            while (channelIsOpen) {
                TelegramMessage message = messageOutputQueue.take();

                telegramClient.sendMessage(message);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


    private void parseCommand(String text) {
        // Позже здесь появятся парсинг команды
        // и создание Subscription.
    }
}