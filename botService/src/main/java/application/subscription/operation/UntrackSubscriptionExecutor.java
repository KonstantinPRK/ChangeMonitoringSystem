package application.subscription.operation;

import application.config.SubscriptionProperties;
import application.subscription.api.SubscriptionAction;
import application.subscription.api.SubscriptionQueueReceipt;
import application.subscription.api.SubscriptionRequest;
import application.subscription.api.SubscriptionRequestFailedException;
import application.subscription.api.SubscriptionServiceApiClient;
import application.subscription.cache.SubscriptionListLoader;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class UntrackSubscriptionExecutor implements SubscriptionOperationExecutor {
    private final SubscriptionServiceApiClient apiClient;
    private final SubscriptionRequestFactory requestFactory;
    private final SubscriptionListLoader listLoader;
    private final SubscriptionProperties subscriptionProperties;


    public UntrackSubscriptionExecutor(
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


    @Override
    public SubscriptionOperationType type() {
        return SubscriptionOperationType.UNTRACK;
    }


    @Override
    public CompletionStage<OperationExecution> execute(StoredSubscriptionOperation operation) {
        SubscriptionRequest request = requestFactory.create(operation, SubscriptionAction.DELETE);
        return apiClient.submit(request).thenApply(receipt -> evaluate(operation, receipt));
    }


    private OperationExecution evaluate(
            StoredSubscriptionOperation operation,
            SubscriptionQueueReceipt receipt
    ) {
        if (receipt.failed()) throw new SubscriptionRequestFailedException(receipt.error());
        if (!receipt.completed()) {
            return OperationExecution.retry(
                    subscriptionProperties.requestRetryDelay(),
                    "Ожидание обработки запроса на удаление"
            );
        }

        listLoader.invalidate(operation.user().key());
        return OperationExecution.success();
    }
}
