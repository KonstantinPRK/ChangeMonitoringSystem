package application.commandHandler;

import application.ConversationState;
import application.User;

public class DeleteCommandHandler implements Command {

    @Override
    public String process(
        CommandType commandType,
        User user
    ) {
        return "Аккаунт пользователя и связанные с ним данные будут удалены.";
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }
}
