package infrastructure.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.List;

public final class TelegramApiClient {

    private static final String API_URL = "https://api.telegram.org/bot";
    private static final int LONG_POLLING_TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;

    public TelegramApiClient(String token, HttpClient httpClient, ObjectMapper objectMapper) {
        if (token.isBlank()) {
            throw new IllegalArgumentException(
                "Telegram token must not be blank"
            );
        }

        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiUrl = API_URL + token + "/";
    }

    public CompletableFuture<List<TelegramUpdate>> getUpdates(long offset) {
        GetUpdatesRequest request = new GetUpdatesRequest(
            offset,
            LONG_POLLING_TIMEOUT_SECONDS,
            List.of("message")
        );

        return post(
            "getUpdates",
            request,
            Duration.ofSeconds(LONG_POLLING_TIMEOUT_SECONDS + 5),
            new TypeReference<>() {}
        );
    }

    public CompletableFuture<Void> sendMessage(String chatId, String text) {
        SendMessageRequest request = new SendMessageRequest(chatId, text);
        TypeReference<TelegramResponse<JsonNode>> responseType = new TypeReference<>() {};

        return post(
            "sendMessage",
            request,
            Duration.ofSeconds(10),
            responseType
        ).thenApply(result -> null);
    }

    private <T> CompletableFuture<T> post(
        String method,
        Object requestBody,
        Duration timeout,
        TypeReference<TelegramResponse<T>> responseType
    ) {
        String json;

        try {
            json = objectMapper.writeValueAsString(requestBody);

        } catch (JsonProcessingException exception) {
            return CompletableFuture.failedFuture(
                new TelegramApiException("Cannot serialize Telegram request", exception));

        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl + method))
            .timeout(timeout)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        return httpClient.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString()
        ).thenApply(response -> readResponse(response, responseType));
    }

    private <T> T readResponse(
        HttpResponse<String> response,
        TypeReference<TelegramResponse<T>> responseType
    ) {
        TelegramResponse<T> telegramResponse;

        try {
            telegramResponse = objectMapper.readValue(
                response.body(),
                responseType
            );
        } catch (JsonProcessingException exception) {
            throw new TelegramApiException(
                "Cannot parse Telegram response",
                exception
            );
        }

        if (response.statusCode() != 200 || !telegramResponse.ok()) {
            throw new TelegramApiException(telegramResponse.description());
        }

        return telegramResponse.result();
    }

    private record GetUpdatesRequest(
        long offset,
        int timeout,
        @JsonProperty("allowed_updates") List<String> allowedUpdates
    ) {
    }

    private record SendMessageRequest(
        @JsonProperty("chat_id") String chatId,
        String text
    ) {
    }

    private record TelegramResponse<T>(
        boolean ok,
        T result,
        @JsonProperty("error_code") Integer errorCode,
        String description
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TelegramUpdate(
        @JsonProperty("update_id") long updateId,
        TelegramIncomingMessage message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TelegramIncomingMessage(
        @JsonProperty("message_id") long messageId,
        TelegramUser from,
        TelegramChat chat,
        String text
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TelegramUser(
        long id,
        @JsonProperty("is_bot") boolean bot,
        String username
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TelegramChat(
        long id,
        String type
    ) {
    }

    public static final class TelegramApiException
        extends RuntimeException {

        public TelegramApiException(String message) {
            super(message);
        }

        public TelegramApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
