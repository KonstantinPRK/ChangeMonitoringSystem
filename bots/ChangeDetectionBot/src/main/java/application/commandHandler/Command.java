package application.commandHandler;

import application.ConversationState;
import application.User;

public interface Command {
    String process(CommandType commandType, User user);

    ConversationState getConversationState();
}
