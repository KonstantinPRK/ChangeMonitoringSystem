package application.subscription.api;

import application.config.BotProperties;
import application.config.SubscriptionProperties;
import application.user.UserKey;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Формирует запросы внешнего протокола для {@code SubscriptionHttpRequestFactory}.
 */
@Component
public class SubscriptionHttpRequestFactory {
    private static final String JSON = "application/json";
    private final SubscriptionProperties subscriptionProperties;
    private final BotProperties botProperties;
    private final ObjectMapper objectMapper;


    public SubscriptionHttpRequestFactory(
            SubscriptionProperties subscriptionProperties,
            BotProperties botProperties,
            ObjectMapper objectMapper
    ) {
        this.subscriptionProperties = subscriptionProperties;
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
    }


    public HttpRequest register(String botId) {
        BotRegistrationRequest registration = new BotRegistrationRequest(
                botId,
                botProperties.baseUrl()
        );
        return jsonRequest("/internal/bots", "POST", registration);
    }


    public HttpRequest confirmAvailability(String botId) {
        return emptyRequest(
                "/internal/bots/" + encode(botId) + "/availability",
                "PUT"
        );
    }


    public HttpRequest unregister(String botId) {
        return emptyRequest("/internal/bots/" + encode(botId), "DELETE");
    }


    public HttpRequest submit(SubscriptionRequest request) {
        return jsonRequest("/api/v1/subscriptions/requests", "POST", request);
    }


    public HttpRequest submitBatch(List<SubscriptionRequest> requests) {
        return jsonRequest("/api/v1/subscriptions/requests/batch", "POST", requests);
    }


    public HttpRequest list(UserKey user, int offset, int limit) {
        String query = "?botId=" + encode(user.botId())
                + "&userId=" + encode(user.externalUserId())
                + "&chatId=" + encode(user.chatId())
                + "&offset=" + offset
                + "&limit=" + limit;
        return requestBuilder("/api/v1/subscriptions" + query).GET().build();
    }


    private HttpRequest jsonRequest(String path, String method, Object body) {
        String json = objectMapper.writeValueAsString(body);
        return requestBuilder(path)
                .header("Content-Type", JSON)
                .method(method, HttpRequest.BodyPublishers.ofString(json))
                .build();
    }


    private HttpRequest emptyRequest(String path, String method) {
        return requestBuilder(path)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
    }


    private HttpRequest.Builder requestBuilder(String path) {
        URI address = subscriptionProperties.baseUrl().resolve(path);
        HttpRequest.Builder builder = HttpRequest.newBuilder(address).timeout(Duration.ofSeconds(10));
        String token = botProperties.internalApiToken();
        if (token != null && !token.isBlank()) builder.header("Authorization", "Bearer " + token);
        return builder;
    }


    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
