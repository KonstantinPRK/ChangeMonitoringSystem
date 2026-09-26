package application.notification.transport;

import application.notification.model.StoredNotification;
import application.catalog.api.SubscriptionRequestBuilder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;

/**
 * Публикует данные во внешний транспорт через {@code HttpLinkNotificationPublisher}.
 */
@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP", matchIfMissing = true)
public class HttpLinkNotificationPublisher implements LinkNotificationPublisher {
    private final HttpClient httpClient;
    private final SubscriptionRequestBuilder requestBuilder;
    private final NotificationReceiptReader receiptReader;


    public HttpLinkNotificationPublisher(
            HttpClient httpClient,
            SubscriptionRequestBuilder requestBuilder,
            NotificationReceiptReader receiptReader
    ) {
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
        this.receiptReader = receiptReader;
    }


    @Override
    public CompletionStage<PublicationResult> publish(StoredNotification notification) {
        HttpRequest request = requestBuilder.create("/api/v1/updates")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(notification.payloadJson()))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(receiptReader::read);
    }
}
