package application.bot;

public interface BotClient {
    void sendNotification(
        BotInstance bot,
        String localUserId,
        String text
    );


    default void close() {
    }
}
