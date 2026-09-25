package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.user.BotUser;

public interface CommandHandler {
    CommandType command();

    InteractionResult handle(BotUser user, ConversationSession session);
}
