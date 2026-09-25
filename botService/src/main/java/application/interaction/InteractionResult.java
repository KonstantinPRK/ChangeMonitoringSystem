package application.interaction;

import application.subscription.operation.SubscriptionOperationDraft;

import java.util.List;

public record InteractionResult(
        ConversationSession session,
        List<String> replies,
        List<SubscriptionOperationDraft> operations
) {
    public static InteractionResult reply(ConversationSession session, String reply) {
        return new InteractionResult(session, List.of(reply), List.of());
    }


    public static InteractionResult operation(
            ConversationSession session,
            String reply,
            SubscriptionOperationDraft operation
    ) {
        return new InteractionResult(session, List.of(reply), List.of(operation));
    }
}
