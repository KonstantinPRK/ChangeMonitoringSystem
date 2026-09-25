package application.subscription.operation;

import application.outbox.OutgoingMessageService;
import application.user.UserManager;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class DeleteAccountExecutor implements SubscriptionOperationExecutor {
    private final RemoveAllSubscriptions removeAllSubscriptions;
    private final UserManager userManager;
    private final OutgoingMessageService outgoingMessageService;


    public DeleteAccountExecutor(
            RemoveAllSubscriptions removeAllSubscriptions,
            UserManager userManager,
            OutgoingMessageService outgoingMessageService
    ) {
        this.removeAllSubscriptions = removeAllSubscriptions;
        this.userManager = userManager;
        this.outgoingMessageService = outgoingMessageService;
    }


    @Override
    public SubscriptionOperationType type() {
        return SubscriptionOperationType.DELETE_ACCOUNT;
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

        userManager.delete(operation.user());
        outgoingMessageService.enqueue(
                "delete-account:" + operation.id(),
                operation.user(),
                "Пользователь и все его подписки удалены."
        );
        return result;
    }
}
