package infrastructure.bot.http;

import application.bot.BotClient;
import application.bot.BotInstance;
import infrastructure.http.RemoteServiceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HttpBotClient implements BotClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;


    public HttpBotClient(
        HttpClient httpClient,
        ObjectMapper objectMapper,
        Duration requestTimeout
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.requestTimeout = requirePositive(requestTimeout);
    }


    @Override
    public void sendNotification(BotInstance bot, String localUserId, String text) {
        NotificationRequest body = new NotificationRequest(
            requireText(localUserId, "localUserId"),
            requireText(text, "text")
        );

        HttpRequest request = HttpRequest.newBuilder(notificationEndpoint(bot.baseUrl()))
            .timeout(requestTimeout)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(writeJson(body)))
            .build();

        sendExpectingSuccess(request, bot.botId());
    }


    private void sendExpectingSuccess(HttpRequest request, String botId) {
        try {
            HttpResponse<Void> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.discarding()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RemoteServiceException(
                    "Bot " + botId + " returned HTTP " + response.statusCode()
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RemoteServiceException("Notification request was interrupted", exception);

        } catch (IOException exception) {
            throw new RemoteServiceException("Cannot contact bot " + botId, exception);

        }
    }


    private byte[] writeJson(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);

        } catch (JsonProcessingException exception) {
            throw new RemoteServiceException("Cannot serialize notification", exception);

        }
    }


    private static URI notificationEndpoint(URI baseUrl) {
        return baseUrl.resolve("/notifications");
    }


    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }


    private static Duration requirePositive(Duration value) {
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }

        return value;
    }
}
