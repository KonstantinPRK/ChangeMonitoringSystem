package application.notification.transport;

import application.config.SubscriptionServiceProperties;
import application.catalog.api.SubscriptionResponseValidator;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;

@Component
public class NotificationReceiptReader {
    private final SubscriptionResponseValidator responseValidator;
    private final SubscriptionServiceProperties serviceProperties;
    private final ObjectMapper objectMapper;


    public NotificationReceiptReader(
            SubscriptionResponseValidator responseValidator,
            SubscriptionServiceProperties serviceProperties,
            ObjectMapper objectMapper
    ) {
        this.responseValidator = responseValidator;
        this.serviceProperties = serviceProperties;
        this.objectMapper = objectMapper;
    }


    public PublicationResult read(HttpResponse<String> response) {
        responseValidator.requireSuccess(response);
        NotificationQueueReceipt receipt = objectMapper.readValue(
                response.body(),
                NotificationQueueReceipt.class
        );
        return switch (receipt.status()) {
            case "COMPLETED" -> PublicationResult.completed();
            case "FAILED" -> PublicationResult.failed(receipt.error());
            default -> PublicationResult.retry(
                    serviceProperties.requestRetryDelay(),
                    "Waiting for SubscriptionService to process the update"
            );
        };
    }
}
