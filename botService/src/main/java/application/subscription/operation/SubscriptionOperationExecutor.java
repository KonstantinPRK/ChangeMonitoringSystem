package application.subscription.operation;

import java.util.concurrent.CompletionStage;

public interface SubscriptionOperationExecutor {
    SubscriptionOperationType type();

    CompletionStage<OperationExecution> execute(StoredSubscriptionOperation operation);
}
