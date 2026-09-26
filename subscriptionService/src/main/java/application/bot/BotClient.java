package application.bot;

import application.http.RemoteHttpClient;
import application.notification.Notification;

import org.springframework.stereotype.Component;

/**
 * Предоставляет доступ к операциям удалённого сервиса через {@code BotClient}.
 */
@Component
public class BotClient {
    private final BotRouter router;
    private final RemoteHttpClient httpClient;


    public BotClient(BotRouter router, RemoteHttpClient httpClient) {
        this.router = router;
        this.httpClient = httpClient;
    }


    public void send(Notification notification) {
        BotInstance bot = router.route(notification.user().botId());
        httpClient.post(bot.baseUrl(), "/api/v1/notifications", notification);
    }
}
