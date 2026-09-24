package application.commandHandler;

import application.ConversationState;
import application.User;

public class UntrackCommandHandler implements Command {

    @Override
    public String process(
        CommandType commandType,
        User user
    ) {
        return "Отправьте ссылку, которую нужно перестать отслеживать.";
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_TEXT;
    }
}
