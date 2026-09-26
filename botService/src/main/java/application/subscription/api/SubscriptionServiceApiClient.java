package application.subscription.api;

import application.user.UserKey;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Выполняет HTTP-запросы к удалённому сервису для {@code SubscriptionServiceApiClient}.
 */
@Component
public class SubscriptionServiceApiClient {
    private final HttpClient httpClient;
    private final SubscriptionHttpRequestFactory requestFactory;
    private final SubscriptionResponseReader responseReader;


    public SubscriptionServiceApiClient(
            HttpClient httpClient,
            SubscriptionHttpRequestFactory requestFactory,
            SubscriptionResponseReader responseReader
    ) {
        this.httpClient = httpClient;
        this.requestFactory = requestFactory;
        this.responseReader = responseReader;
    }


    public CompletionStage<Void> registerBot(String botId) {
        return sendWithoutBody(requestFactory.register(botId));
    }


    public CompletionStage<Void> confirmAvailability(String botId) {
        return sendWithoutBody(requestFactory.confirmAvailability(botId));
    }


    public CompletionStage<Void> unregisterBot(String botId) {
        return sendWithoutBody(requestFactory.unregister(botId));
    }


    public CompletionStage<SubscriptionQueueReceipt> submit(SubscriptionRequest request) {
        return httpClient.sendAsync(
                        requestFactory.submit(request),
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(responseReader::readReceipt);
    }


    public CompletionStage<Void> submitBatch(List<SubscriptionRequest> requests) {
        return sendWithoutBody(requestFactory.submitBatch(requests));
    }


    public CompletionStage<List<SubscriptionView>> list(UserKey user, int offset, int limit) {
        return httpClient.sendAsync(
                        requestFactory.list(user, offset, limit),
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(responseReader::readSubscriptions);
    }


    private CompletionStage<Void> sendWithoutBody(java.net.http.HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(responseReader::requireSuccess);
    }
}
