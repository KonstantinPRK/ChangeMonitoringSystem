package application.commandHandler;

import application.ConversationState;
import application.User;

public class TrackCommandHandler implements Command {

    @Override
    public String process(
        CommandType commandType,
        User user
    ) {
        return "Отправьте ссылку, которую нужно отслеживать.";
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_TEXT;
    }
}
