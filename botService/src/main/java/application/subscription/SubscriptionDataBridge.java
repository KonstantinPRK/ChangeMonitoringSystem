package application.subscription;

import application.notification.BotNotification;
import application.notification.NotificationReceiver;
import application.persistence.AfterCommitAction;
import application.subscription.operation.SubscriptionOperationDraft;
import application.subscription.operation.SubscriptionOperationRepository;
import application.user.BotUser;
import application.work.SubscriptionWorkSignal;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SubscriptionDataBridge {
    private final SubscriptionOperationRepository operationRepository;
    private final NotificationReceiver notificationReceiver;
    private final SubscriptionWorkSignal workSignal;
    private final AfterCommitAction afterCommitAction;


    public SubscriptionDataBridge(
            SubscriptionOperationRepository operationRepository,
            NotificationReceiver notificationReceiver,
            SubscriptionWorkSignal workSignal,
            AfterCommitAction afterCommitAction
    ) {
        this.operationRepository = operationRepository;
        this.notificationReceiver = notificationReceiver;
        this.workSignal = workSignal;
        this.afterCommitAction = afterCommitAction;
    }


    public void enqueue(UUID operationId, BotUser user, SubscriptionOperationDraft operation) {
        operationRepository.save(operationId, user, operation);
        afterCommitAction.execute(workSignal::signal);
    }


    public void accept(BotNotification notification) {
        notificationReceiver.accept(notification);
    }
}
