package infrastructure.tracker.http;

import application.tracker.TrackerClient;
import application.tracker.TrackerInstance;
import infrastructure.http.RemoteServiceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HttpTrackerClient implements TrackerClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;


    public HttpTrackerClient(
        HttpClient httpClient,
        ObjectMapper objectMapper,
        Duration requestTimeout
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.requestTimeout = requirePositive(requestTimeout);
    }


    @Override
    public void addSubscription(
        TrackerInstance tracker,
        URI resource,
        String botId,
        String localUserId
    ) {
        AddSubscriptionRequest body = new AddSubscriptionRequest(
            resource,
            requireText(botId, "botId"),
            requireText(localUserId, "localUserId")
        );

        HttpRequest request = HttpRequest.newBuilder(subscriptionEndpoint(tracker.baseUrl()))
            .timeout(requestTimeout)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(writeJson(body)))
            .build();

        sendExpectingSuccess(request, tracker.trackerId());
    }


    private void sendExpectingSuccess(HttpRequest request, String trackerId) {
        try {
            HttpResponse<Void> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.discarding()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RemoteServiceException(
                    "Tracker " + trackerId + " returned HTTP " + response.statusCode()
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RemoteServiceException("Subscription request was interrupted", exception);

        } catch (IOException exception) {
            throw new RemoteServiceException("Cannot contact tracker " + trackerId, exception);

        }
    }


    private byte[] writeJson(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);

        } catch (JsonProcessingException exception) {
            throw new RemoteServiceException("Cannot serialize subscription request", exception);

        }
    }


    private static URI subscriptionEndpoint(URI baseUrl) {
        return baseUrl.resolve("/subscriptions");
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
