package application.commandHandler;

import application.ConversationState;
import application.User;

public class StopCommandHandler implements Command {

    @Override
    public String process(
        CommandType commandType,
        User user
    ) {
        return "Все отслеживания пользователя будут остановлены.";
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }
}
