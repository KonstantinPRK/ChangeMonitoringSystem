package application.telegram;

import application.config.TelegramProperties;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

@Component
public class TelegramApiClient {
    private static final URI API_BASE_URL = URI.create("https://api.telegram.org/");
    private final HttpClient httpClient;
    private final TelegramProperties telegramProperties;
    private final ObjectMapper objectMapper;


    public TelegramApiClient(
            HttpClient httpClient,
            TelegramProperties telegramProperties,
            ObjectMapper objectMapper
    ) {
        this.httpClient = httpClient;
        this.telegramProperties = telegramProperties;
        this.objectMapper = objectMapper;
    }


    public CompletionStage<JsonNode> post(String method, Object requestBody) {
        HttpRequest request = createRequest(method, requestBody);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::readResponse);
    }


    private HttpRequest createRequest(String method, Object requestBody) {
        URI address = API_BASE_URL.resolve("./bot" + telegramProperties.token() + '/' + method);
        String json = objectMapper.writeValueAsString(requestBody);
        return HttpRequest.newBuilder(address)
                .header("Content-Type", "application/json")
                .timeout(telegramProperties.longPollTimeout().plus(Duration.ofSeconds(10)))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }


    private JsonNode readResponse(HttpResponse<String> response) {
        JsonNode body = objectMapper.readTree(response.body());
        if (response.statusCode() >= 200 && response.statusCode() < 300 && body.path("ok").asBoolean()) {
            return body.path("result");
        }

        int errorCode = body.path("error_code").asInt(response.statusCode());
        String description = body.path("description").asString("Telegram API request failed");
        long retrySeconds = body.path("parameters").path("retry_after").asLong(0L);
        throw new TelegramApiException(errorCode, description, java.time.Duration.ofSeconds(retrySeconds));
    }
}
