package application.commandHandler;

import application.ConversationState;
import application.User;

public class StartCommandHandler implements Command {

    @Override
    public String start(User user) {
        return "Бот готов к работе. Используйте /" +
            CommandType.HELP.code() +
            ", чтобы посмотреть доступные команды.";
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
