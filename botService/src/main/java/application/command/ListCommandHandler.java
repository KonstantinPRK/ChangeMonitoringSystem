package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.subscription.operation.SubscriptionOperationDraft;
import application.subscription.operation.SubscriptionOperationType;
import application.user.BotUser;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code ListCommandHandler}.
 */
@Component
public class ListCommandHandler implements CommandHandler {
    @Override
    public CommandType command() {
        return CommandType.LIST;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.operation(
                session,
                "Получаю список подписок…",
                SubscriptionOperationDraft.simple(SubscriptionOperationType.LIST)
        );
    }
}
