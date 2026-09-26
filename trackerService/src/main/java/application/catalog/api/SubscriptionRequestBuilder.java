package application.catalog.api;

import application.config.SubscriptionServiceProperties;
import application.config.TrackerProperties;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

/**
 * Формирует запросы внешнего протокола для {@code SubscriptionRequestBuilder}.
 */
@Component
public class SubscriptionRequestBuilder {
    private final SubscriptionServiceProperties serviceProperties;
    private final TrackerProperties trackerProperties;


    public SubscriptionRequestBuilder(
            SubscriptionServiceProperties serviceProperties,
            TrackerProperties trackerProperties
    ) {
        this.serviceProperties = serviceProperties;
        this.trackerProperties = trackerProperties;
    }


    public HttpRequest.Builder create(String path) {
        URI address = serviceProperties.baseUrl().resolve(path);
        HttpRequest.Builder builder = HttpRequest.newBuilder(address).timeout(Duration.ofSeconds(10));

        String token = trackerProperties.internalApiToken();
        if (token != null && !token.isBlank()) builder.header("Authorization", "Bearer " + token);

        return builder;
    }
}
