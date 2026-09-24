package application.commandHandler;

import application.ConversationState;
import application.User;

public class ListCommandHandler implements Command {

    @Override
    public String process(
        CommandType commandType,
        User user
    ) {
        return "Запрашиваю список отслеживаемых ссылок.";
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }
}
