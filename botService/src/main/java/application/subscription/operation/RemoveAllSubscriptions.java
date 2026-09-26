package application.subscription.operation;

import application.config.SubscriptionProperties;
import application.subscription.api.SubscriptionRequest;
import application.subscription.api.SubscriptionServiceApiClient;
import application.subscription.api.SubscriptionView;
import application.subscription.cache.SubscriptionListLoader;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Реализует ответственность компонента {@code RemoveAllSubscriptions}.
 */
@Component
public class RemoveAllSubscriptions {
    private final SubscriptionServiceApiClient apiClient;
    private final SubscriptionRequestFactory requestFactory;
    private final SubscriptionListLoader listLoader;
    private final SubscriptionProperties subscriptionProperties;


    public RemoveAllSubscriptions(
            SubscriptionServiceApiClient apiClient,
            SubscriptionRequestFactory requestFactory,
            SubscriptionListLoader listLoader,
            SubscriptionProperties subscriptionProperties
    ) {
        this.apiClient = apiClient;
        this.requestFactory = requestFactory;
        this.listLoader = listLoader;
        this.subscriptionProperties = subscriptionProperties;
    }


    public CompletionStage<OperationExecution> execute(StoredSubscriptionOperation operation) {
        return listLoader.loadFresh(operation.user().key())
                .thenCompose(subscriptions -> remove(operation, subscriptions));
    }


    private CompletionStage<OperationExecution> remove(
            StoredSubscriptionOperation operation,
            List<SubscriptionView> subscriptions
    ) {
        if (subscriptions.isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(OperationExecution.success());
        }

        List<SubscriptionRequest> requests = requestFactory.createDeletes(operation, subscriptions);
        return apiClient.submitBatch(requests).thenApply(ignored -> {
            listLoader.invalidate(operation.user().key());
            return OperationExecution.retry(
                    subscriptionProperties.requestRetryDelay(),
                    "Ожидание применения запросов на удаление"
            );
        });
    }
}
