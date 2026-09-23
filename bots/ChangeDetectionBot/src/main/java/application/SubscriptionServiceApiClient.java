package application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SubscriptionServiceApiClient {
    private static final String INTERACTIONS_PATH = "/api/v1/interactions";

    private final String botId;
    private final URI baseUrl;
    private final Duration requestTimeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;


    public SubscriptionServiceApiClient(
            String botId,
            URI baseUrl,
            Duration requestTimeout,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        if (botId == null || botId.isBlank()) {
            throw new IllegalArgumentException(
                    "botId must not be blank"
            );
        }

        if (baseUrl == null
                || !baseUrl.isAbsolute()
                || baseUrl.getHost() == null) {
            throw new IllegalArgumentException(
                    "baseUrl must be an absolute network URI"
            );
        }

        if (requestTimeout == null
                || requestTimeout.isZero()
                || requestTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "requestTimeout must be positive"
            );
        }

        this.botId = botId;
        this.baseUrl = baseUrl;
        this.requestTimeout = requestTimeout;
        this.httpClient = Objects.requireNonNull(
                httpClient,
                "httpClient"
        );

        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper"
        );
    }


    public CompletableFuture<BotReply> sendUserMessage(
            String senderId,
            String chatId,
            String text
    ) {
        BotInteractionRequest requestBody =
                new BotInteractionRequest(
                        botId,
                        senderId,
                        chatId,
                        text
                );

        byte[] json;

        try {
            json = objectMapper.writeValueAsBytes(
                    requestBody
            );

        } catch (JsonProcessingException exception) {
            return CompletableFuture.failedFuture(
                    new SubscriptionServiceApiException(
                            "Cannot serialize subscription service request",
                            exception
                    )
            );
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseUrl.resolve(INTERACTIONS_PATH))
                .timeout(requestTimeout)
                .header(
                        "Content-Type",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofByteArray(json)
                )
                .build();

        return httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
        ).thenApply(this::readReply);
    }


    private BotReply readReply(
            HttpResponse<String> response
    ) {
        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new SubscriptionServiceApiException(
                    response.statusCode(),
                    "Subscription service returned HTTP "
                            + response.statusCode()
            );
        }

        if (response.body() == null || response.body().isBlank()) {
        }

        try {
            return objectMapper.readValue(
                    response.body(),
                    BotReply.class
            );

        } catch (JsonProcessingException exception) {
            throw new SubscriptionServiceApiException(
                    response.statusCode(),
                    "Cannot parse subscription service response",
                    exception
            );
        }
    }
}