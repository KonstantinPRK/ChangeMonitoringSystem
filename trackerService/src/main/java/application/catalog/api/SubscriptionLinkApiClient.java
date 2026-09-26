package application.catalog.api;

import application.config.TrackerProperties;
import application.catalog.model.TrackedLink;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Выполняет HTTP-запросы к удалённому сервису для {@code SubscriptionLinkApiClient}.
 */
@Component
public class SubscriptionLinkApiClient {
    private final HttpClient httpClient;
    private final SubscriptionRequestBuilder requestBuilder;
    private final SubscriptionLinkResponseReader responseReader;
    private final TrackerProperties trackerProperties;


    public SubscriptionLinkApiClient(
            HttpClient httpClient,
            SubscriptionRequestBuilder requestBuilder,
            SubscriptionLinkResponseReader responseReader,
            TrackerProperties trackerProperties
    ) {
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
        this.responseReader = responseReader;
        this.trackerProperties = trackerProperties;
    }


    public CompletionStage<List<TrackedLink>> load(long afterId, int limit) {
        String trackerId = URLEncoder.encode(trackerProperties.id(), StandardCharsets.UTF_8);
        String path = "/api/v1/trackers/" + trackerId + "/links?afterId=" + afterId + "&limit=" + limit;

        HttpRequest request = requestBuilder.create(path).GET().build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(responseReader::read);
    }
}
