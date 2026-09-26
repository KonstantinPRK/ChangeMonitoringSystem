package application.subscription.operation;

import application.outbox.OutgoingMessageService;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

/**
 * Выполняет прикладную операцию через {@code StopSubscriptionsExecutor}.
 */
@Component
public class StopSubscriptionsExecutor implements SubscriptionOperationExecutor {
    private final RemoveAllSubscriptions removeAllSubscriptions;
    private final OutgoingMessageService outgoingMessageService;


    public StopSubscriptionsExecutor(
            RemoveAllSubscriptions removeAllSubscriptions,
            OutgoingMessageService outgoingMessageService
    ) {
        this.removeAllSubscriptions = removeAllSubscriptions;
        this.outgoingMessageService = outgoingMessageService;
    }


    @Override
    public SubscriptionOperationType type() {
        return SubscriptionOperationType.STOP;
    }


    @Override
    public CompletionStage<OperationExecution> execute(StoredSubscriptionOperation operation) {
        return removeAllSubscriptions.execute(operation).thenApply(result -> complete(operation, result));
    }


    private OperationExecution complete(
            StoredSubscriptionOperation operation,
            OperationExecution result
    ) {
        if (!result.completed()) return result;

        outgoingMessageService.enqueue(
                "stop:" + operation.id(),
                operation.user(),
                "Все подписки удалены."
        );
        return result;
    }
}
