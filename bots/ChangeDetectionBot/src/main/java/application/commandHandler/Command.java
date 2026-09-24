package application.commandHandler;

import application.ConversationState;
import application.User;

public interface Command {
    String start(User user);

    String process(User user, String message);

    ConversationState getConversationState();

    boolean isCompleted();
}
