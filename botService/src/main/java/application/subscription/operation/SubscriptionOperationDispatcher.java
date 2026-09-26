package application.subscription.operation;

import application.work.SerializedWorker;
import application.work.SubscriptionWorkSignal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Передаёт ожидающие задачи подходящим обработчикам через {@code SubscriptionOperationDispatcher}.
 */
@Component
public class SubscriptionOperationDispatcher {
    private final SubscriptionOperationRepository operationRepository;
    private final SubscriptionOperationExecutors operationExecutors;
    private final SubscriptionOperationCompletion operationCompletion;
    private final SerializedWorker worker;
    private final AtomicBoolean running = new AtomicBoolean();


    public SubscriptionOperationDispatcher(
            SubscriptionOperationRepository operationRepository,
            SubscriptionOperationExecutors operationExecutors,
            SubscriptionOperationCompletion operationCompletion,
            SubscriptionWorkSignal workSignal,
            @Qualifier("botTaskExecutor") Executor executor
    ) {
        this.operationRepository = operationRepository;
        this.operationExecutors = operationExecutors;
        this.operationCompletion = operationCompletion;
        worker = new SerializedWorker(executor, this::dispatchNext);
        workSignal.connect(worker::signal);
    }


    public void start() {
        running.set(true);
        worker.signal();
    }


    public void stop() {
        running.set(false);
    }


    @Scheduled(fixedDelayString = "${app.workers.recovery-interval}")
    public void recover() {
        if (running.get()) worker.signal();
    }


    private CompletionStage<Boolean> dispatchNext() {
        if (!running.get()) return CompletableFuture.completedFuture(false);
        Optional<StoredSubscriptionOperation> claimedOperation = operationRepository.claimNext();
        if (claimedOperation.isEmpty()) return CompletableFuture.completedFuture(false);

        StoredSubscriptionOperation operation = claimedOperation.get();
        SubscriptionOperationExecutor executor = operationExecutors.get(operation.type());
        return executor.execute(operation)
                .handle((result, failure) -> complete(operation, result, failure));
    }


    private boolean complete(
            StoredSubscriptionOperation operation,
            OperationExecution result,
            Throwable failure
    ) {
        if (failure != null) {
            operationCompletion.fail(operation, unwrap(failure));
            return true;
        }
        operationCompletion.complete(operation, result);
        return true;
    }


    private Throwable unwrap(Throwable failure) {
        if (failure instanceof CompletionException && failure.getCause() != null) return failure.getCause();
        return failure;
    }
}
