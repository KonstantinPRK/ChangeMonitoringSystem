package application.outbox;

import application.persistence.AfterCommitAction;
import application.user.BotUser;
import application.work.OutgoingWorkSignal;

import org.springframework.stereotype.Service;

@Service
public class OutgoingMessageService {
    private final OutgoingMessageRepository messageRepository;
    private final OutgoingWorkSignal workSignal;
    private final AfterCommitAction afterCommitAction;


    public OutgoingMessageService(
            OutgoingMessageRepository messageRepository,
            OutgoingWorkSignal workSignal,
            AfterCommitAction afterCommitAction
    ) {
        this.messageRepository = messageRepository;
        this.workSignal = workSignal;
        this.afterCommitAction = afterCommitAction;
    }


    public void enqueue(String deduplicationKey, BotUser user, String text) {
        messageRepository.save(deduplicationKey, user.id(), user.key().chatId(), text);
        afterCommitAction.execute(workSignal::signal);
    }
}
