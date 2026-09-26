package application.outbox;

import application.messenger.DeliveryReceipt;
import application.messenger.MessengerClient;
import application.messenger.MessengerClientRegistry;
import application.messenger.MessengerDataBridge;
import application.messenger.OutgoingMessage;
import application.work.OutgoingWorkSignal;
import application.work.SerializedWorker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Передаёт ожидающие задачи подходящим обработчикам через {@code OutgoingMessageDispatcher}.
 */
@Component
public class OutgoingMessageDispatcher {
    private final OutgoingMessageRepository messageRepository;
    private final MessengerDataBridge messengerDataBridge;
    private final OutgoingMessageFailurePolicy failurePolicy;
    private final Map<String, SerializedWorker> workersByBotId;
    private final Counter sentMessages;
    private final AtomicBoolean running = new AtomicBoolean();


    public OutgoingMessageDispatcher(
            OutgoingMessageRepository messageRepository,
            MessengerDataBridge messengerDataBridge,
            MessengerClientRegistry clientRegistry,
            OutgoingMessageFailurePolicy failurePolicy,
            OutgoingWorkSignal workSignal,
            @Qualifier("botTaskExecutor") Executor executor,
            MeterRegistry meterRegistry
    ) {
        this.messageRepository = messageRepository;
        this.messengerDataBridge = messengerDataBridge;
        this.failurePolicy = failurePolicy;
        workersByBotId = createWorkers(clientRegistry, executor);
        workSignal.connect(this::signalWorkers);
        sentMessages = meterRegistry.counter("bot_sent_messages_total");
    }


    public void start() {
        running.set(true);
        signalWorkers();
    }


    public void stop() {
        running.set(false);
    }


    @Scheduled(fixedDelayString = "${app.workers.recovery-interval}")
    public void recover() {
        if (running.get()) signalWorkers();
    }


    private Map<String, SerializedWorker> createWorkers(
            MessengerClientRegistry clientRegistry,
            Executor executor
    ) {
        Map<String, SerializedWorker> workers = new LinkedHashMap<>();
        for (MessengerClient client : clientRegistry.clients()) {
            String botId = client.botId();
            workers.put(botId, new SerializedWorker(executor, () -> dispatchNext(botId)));
        }
        return Map.copyOf(workers);
    }


    private void signalWorkers() {
        for (SerializedWorker worker : workersByBotId.values()) worker.signal();
    }


    private CompletionStage<Boolean> dispatchNext(String botId) {
        if (!running.get()) return CompletableFuture.completedFuture(false);
        Optional<StoredOutgoingMessage> claimedMessage = messageRepository.claimNext(botId);
        if (claimedMessage.isEmpty()) return CompletableFuture.completedFuture(false);

        StoredOutgoingMessage message = claimedMessage.get();
        OutgoingMessage outgoingMessage = new OutgoingMessage(
                message.id(),
                message.botId(),
                message.chatId(),
                message.text()
        );
        return messengerDataBridge.send(outgoingMessage)
                .handle((receipt, failure) -> complete(message, receipt, failure));
    }


    private boolean complete(
            StoredOutgoingMessage message,
            DeliveryReceipt receipt,
            Throwable failure
    ) {
        if (failure == null) {
            messageRepository.complete(message, receipt.messengerMessageId());
            sentMessages.increment();
            return true;
        }

        handleFailure(message, unwrap(failure));
        return true;
    }


    private void handleFailure(StoredOutgoingMessage message, Throwable failure) {
        if (failurePolicy.isPermanent(failure)) {
            messageRepository.fail(message, failure.getMessage());
            return;
        }

        messageRepository.retry(
                message,
                failurePolicy.retryDelay(message, failure),
                failure.getMessage()
        );
    }


    private Throwable unwrap(Throwable failure) {
        if (failure instanceof CompletionException && failure.getCause() != null) return failure.getCause();
        return failure;
    }
}
