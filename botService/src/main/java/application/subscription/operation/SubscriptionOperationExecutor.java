package application.subscription.operation;

import java.util.concurrent.CompletionStage;

/**
 * Выполняет прикладную операцию через {@code SubscriptionOperationExecutor}.
 */
public interface SubscriptionOperationExecutor {
    /**
     * Возвращает тип долговечной операции, которую поддерживает обработчик.
     *
     * @return поддерживаемый тип операции
     */
    SubscriptionOperationType type();


    /**
     * Асинхронно выполняет одну захваченную операцию с подпиской.
     *
     * @param operation сохранённая операция для выполнения
     * @return асинхронный результат выполнения
     */
    CompletionStage<OperationExecution> execute(StoredSubscriptionOperation operation);
}
