package application.registration;

import application.config.TrackerProperties;
import application.connector.ConnectorRegistry;
import application.catalog.api.SubscriptionRequestBuilder;
import application.catalog.api.SubscriptionResponseValidator;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

/**
 * Выполняет HTTP-запросы к удалённому сервису для {@code TrackerRegistrationApiClient}.
 */
@Component
public class TrackerRegistrationApiClient {
    private final HttpClient httpClient;
    private final SubscriptionRequestBuilder requestBuilder;
    private final SubscriptionResponseValidator responseValidator;
    private final TrackerProperties trackerProperties;
    private final ConnectorRegistry connectorRegistry;
    private final ObjectMapper objectMapper;


    public TrackerRegistrationApiClient(
            HttpClient httpClient,
            SubscriptionRequestBuilder requestBuilder,
            SubscriptionResponseValidator responseValidator,
            TrackerProperties trackerProperties,
            ConnectorRegistry connectorRegistry,
            ObjectMapper objectMapper
    ) {
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
        this.responseValidator = responseValidator;
        this.trackerProperties = trackerProperties;
        this.connectorRegistry = connectorRegistry;
        this.objectMapper = objectMapper;
    }


    public CompletionStage<Void> register() {
        TrackerRegistrationRequest registration = new TrackerRegistrationRequest(
                trackerProperties.id(),
                trackerProperties.baseUrl(),
                connectorRegistry.supportedHosts()
        );
        String json = objectMapper.writeValueAsString(registration);
        HttpRequest request = requestBuilder.create("/internal/trackers")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(request);
    }


    public CompletionStage<Void> confirmAvailability() {
        String path = "/internal/trackers/" + trackerId() + "/availability";
        HttpRequest request = requestBuilder.create(path)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request);
    }


    public CompletionStage<Void> unregister() {
        HttpRequest request = requestBuilder.create("/internal/trackers/" + trackerId())
                .DELETE()
                .build();
        return send(request);
    }


    private CompletionStage<Void> send(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(responseValidator::requireSuccess);
    }


    private String trackerId() {
        return URLEncoder.encode(trackerProperties.id(), StandardCharsets.UTF_8);
    }
}
