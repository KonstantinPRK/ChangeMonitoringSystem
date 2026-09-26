package application.subscription.operation;

import application.outbox.OutgoingMessageService;
import application.subscription.cache.SubscriptionListFormatter;
import application.subscription.cache.SubscriptionListLoader;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

/**
 * Выполняет прикладную операцию через {@code ListSubscriptionsExecutor}.
 */
@Component
public class ListSubscriptionsExecutor implements SubscriptionOperationExecutor {
    private final SubscriptionListLoader listLoader;
    private final SubscriptionListFormatter listFormatter;
    private final OutgoingMessageService outgoingMessageService;


    public ListSubscriptionsExecutor(
            SubscriptionListLoader listLoader,
            SubscriptionListFormatter listFormatter,
            OutgoingMessageService outgoingMessageService
    ) {
        this.listLoader = listLoader;
        this.listFormatter = listFormatter;
        this.outgoingMessageService = outgoingMessageService;
    }


    @Override
    public SubscriptionOperationType type() {
        return SubscriptionOperationType.LIST;
    }


    @Override
    public CompletionStage<OperationExecution> execute(StoredSubscriptionOperation operation) {
        return listLoader.load(operation.user().key()).thenApply(subscriptions -> {
            String text = listFormatter.format(subscriptions);
            outgoingMessageService.enqueue("list:" + operation.id(), operation.user(), text);
            return OperationExecution.success();
        });
    }
}
