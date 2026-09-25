package application.interaction;

import application.user.BotUser;

public interface ConversationStepHandler {
    ConversationState state();

    InteractionResult handle(BotUser user, ConversationSession session, String text);
}
