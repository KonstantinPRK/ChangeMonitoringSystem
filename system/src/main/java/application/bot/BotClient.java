package application.bot;

public interface BotClient {
    void sendNotification(
        BotDescriptor bot,
        String localUserId,
        String text
    );
}

