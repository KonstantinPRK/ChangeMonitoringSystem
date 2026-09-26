package application.subscription.operation;

import application.outbox.OutgoingMessageService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Завершает обработку и фиксирует её результат через {@code SubscriptionOperationCompletion}.
 */
@Component
public class SubscriptionOperationCompletion {
    private final SubscriptionOperationRepository operationRepository;
    private final OperationFailurePolicy failurePolicy;
    private final OutgoingMessageService outgoingMessageService;


    public SubscriptionOperationCompletion(
            SubscriptionOperationRepository operationRepository,
            OperationFailurePolicy failurePolicy,
            OutgoingMessageService outgoingMessageService
    ) {
        this.operationRepository = operationRepository;
        this.failurePolicy = failurePolicy;
        this.outgoingMessageService = outgoingMessageService;
    }


    @Transactional
    public void complete(StoredSubscriptionOperation operation, OperationExecution result) {
        if (result.completed()) operationRepository.complete(operation);
        else operationRepository.retry(operation, result.retryAfter(), result.reason());
    }


    @Transactional
    public void fail(StoredSubscriptionOperation operation, Throwable failure) {
        OperationFailureDecision decision = failurePolicy.decide(operation, failure);
        if (decision.retry()) {
            operationRepository.retry(operation, decision.retryAfter(), failure.getMessage());
            return;
        }

        operationRepository.fail(operation, failure.getMessage());
        outgoingMessageService.enqueue(
                "operation-failed:" + operation.id(),
                operation.user(),
                "Не удалось выполнить операцию. Попробуйте ещё раз позже."
        );
    }
}
