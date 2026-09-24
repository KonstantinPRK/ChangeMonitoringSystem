package application.commandHandler;

import application.ConversationState;
import application.User;

public class StartCommandHandler implements Command {

    @Override
    public String process(CommandType commandType, User user) {
        return "Бот готов к работе. Используйте /help, чтобы посмотреть доступные команды.";
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }
}
