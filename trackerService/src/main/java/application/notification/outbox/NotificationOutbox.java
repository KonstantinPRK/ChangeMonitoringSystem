package application.notification.outbox;

import application.snapshot.ResourceSnapshot;
import application.transaction.AfterCommitAction;
import application.notification.creation.NotificationEventFactory;
import application.notification.model.NotificationEvent;
import application.catalog.model.TrackedResource;
import application.work.NotificationWorkSignal;

import org.springframework.stereotype.Component;

/**
 * Реализует ответственность компонента {@code NotificationOutbox}.
 */
@Component
public class NotificationOutbox {
    private final NotificationEventFactory eventFactory;
    private final NotificationOutboxRepository outboxRepository;
    private final NotificationWorkSignal workSignal;
    private final AfterCommitAction afterCommitAction;


    public NotificationOutbox(
            NotificationEventFactory eventFactory,
            NotificationOutboxRepository outboxRepository,
            NotificationWorkSignal workSignal,
            AfterCommitAction afterCommitAction
    ) {
        this.eventFactory = eventFactory;
        this.outboxRepository = outboxRepository;
        this.workSignal = workSignal;
        this.afterCommitAction = afterCommitAction;
    }


    public void enqueue(TrackedResource resource, ResourceSnapshot snapshot) {
        NotificationEvent event = eventFactory.create(resource, snapshot);
        outboxRepository.save(resource.id(), event);
        afterCommitAction.execute(workSignal::signal);
    }
}
