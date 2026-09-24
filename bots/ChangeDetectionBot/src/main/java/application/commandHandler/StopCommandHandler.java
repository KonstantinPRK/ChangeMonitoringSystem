package application.commandHandler;

import application.ConversationState;
import application.User;

public class StopCommandHandler implements Command {

    @Override
    public String start(User user) {
        return "Все отслеживания пользователя будут остановлены.";
    }


    @Override
    public String process(User user, String message) {
        return start(user);
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }


    @Override
    public boolean isCompleted() {
        return true;
    }
}
