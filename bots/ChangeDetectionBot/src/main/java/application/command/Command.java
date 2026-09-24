package application.command;

import application.interaction.ConversationState;
import application.user.User;

public interface Command {
    CommandResult start(User user);


    CommandResult process(User user, String message);


    ConversationState getConversationState();


    boolean isCompleted();
}
