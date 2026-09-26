package application.interaction;

import application.subscription.operation.SubscriptionOperationDraft;
import application.subscription.operation.SubscriptionOperationType;
import application.user.BotUser;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code DeleteConfirmationStepHandler}.
 */
@Component
public class DeleteConfirmationStepHandler implements ConversationStepHandler {
    @Override
    public ConversationState state() {
        return ConversationState.WAITING_DELETE_CONFIRMATION;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session, String text) {
        ConversationSession initialSession = ConversationSession.initial(user.id());
        if (!"DELETE".equals(text.trim())) {
            return InteractionResult.reply(initialSession, "Удаление отменено.");
        }

        return InteractionResult.operation(
                initialSession,
                "Удаляю пользователя и его подписки.",
                SubscriptionOperationDraft.simple(SubscriptionOperationType.DELETE_ACCOUNT)
        );
    }
}
