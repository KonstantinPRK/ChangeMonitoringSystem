package application.inbox;

import application.messenger.IncomingMessageBatch;
import application.persistence.AfterCommitAction;
import application.work.IncomingWorkSignal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncomingMessageAcceptor {
    private final IncomingMessageRepository messageRepository;
    private final IncomingWorkSignal workSignal;
    private final AfterCommitAction afterCommitAction;
    private final Counter receivedMessages;


    public IncomingMessageAcceptor(
            IncomingMessageRepository messageRepository,
            IncomingWorkSignal workSignal,
            AfterCommitAction afterCommitAction,
            MeterRegistry meterRegistry
    ) {
        this.messageRepository = messageRepository;
        this.workSignal = workSignal;
        this.afterCommitAction = afterCommitAction;
        receivedMessages = meterRegistry.counter("bot_user_messages_total");
    }


    @Transactional
    public void accept(IncomingMessageBatch batch) {
        batch.messages().forEach(message -> messageRepository.save(batch.botId(), message));
        messageRepository.saveCheckpoint(batch.botId(), batch.nextOffset());
        receivedMessages.increment(batch.messages().size());
        afterCommitAction.execute(workSignal::signal);
    }
}
