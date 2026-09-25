package application.notification;

import application.bot.BotClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP", matchIfMissing = true)
public class HttpNotificationSender implements NotificationSender {
    private final BotClient botClient;


    public HttpNotificationSender(BotClient botClient) {
        this.botClient = botClient;
    }


    @Override
    public void send(Notification notification) {
        botClient.send(notification);
    }
}
