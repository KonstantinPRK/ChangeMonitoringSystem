package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.subscription.operation.SubscriptionOperationDraft;
import application.subscription.operation.SubscriptionOperationType;
import application.user.BotUser;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code StopCommandHandler}.
 */
@Component
public class StopCommandHandler implements CommandHandler {
    @Override
    public CommandType command() {
        return CommandType.STOP;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.operation(
                session,
                "Удаляю все подписки. Это может занять несколько секунд.",
                SubscriptionOperationDraft.simple(SubscriptionOperationType.STOP)
        );
    }
}
