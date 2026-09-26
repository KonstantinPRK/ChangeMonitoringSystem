package application.inbox;

import application.config.WorkerProperties;
import application.work.IncomingWorkSignal;
import application.work.SerializedWorker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Передаёт ожидающие задачи подходящим обработчикам через {@code IncomingMessageDispatcher}.
 */
@Component
public class IncomingMessageDispatcher {
    private final IncomingMessageRepository messageRepository;
    private final IncomingMessageProcessor messageProcessor;
    private final WorkerProperties workerProperties;
    private final SerializedWorker worker;
    private final AtomicBoolean running = new AtomicBoolean();


    public IncomingMessageDispatcher(
            IncomingMessageRepository messageRepository,
            IncomingMessageProcessor messageProcessor,
            WorkerProperties workerProperties,
            IncomingWorkSignal workSignal,
            @Qualifier("botTaskExecutor") Executor executor
    ) {
        this.messageRepository = messageRepository;
        this.messageProcessor = messageProcessor;
        this.workerProperties = workerProperties;
        worker = new SerializedWorker(executor, this::processNext);
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


    private CompletionStage<Boolean> processNext() {
        if (!running.get()) return CompletableFuture.completedFuture(false);
        Optional<StoredIncomingMessage> claimedMessage = messageRepository.claimNext();
        if (claimedMessage.isEmpty()) return CompletableFuture.completedFuture(false);

        StoredIncomingMessage message = claimedMessage.get();
        try {
            messageProcessor.process(message);

        } catch (RuntimeException exception) {
            completeFailure(message, exception);

        }
        return CompletableFuture.completedFuture(true);
    }


    private void completeFailure(StoredIncomingMessage message, RuntimeException failure) {
        if (message.attempts() >= workerProperties.maximumAttempts()) {
            messageRepository.fail(message, failure.getMessage());
            return;
        }

        messageRepository.retry(message, workerProperties.retryDelay(), failure.getMessage());
    }
}
