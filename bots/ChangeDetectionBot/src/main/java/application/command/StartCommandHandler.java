package application.command;

import application.interaction.ConversationState;
import application.user.User;

public class StartCommandHandler implements Command {
    @Override
    public CommandResult start(User user) {
        String response =
            "Бот готов к работе. Используйте /" +
                CommandType.HELP.code() +
                ", чтобы посмотреть доступные команды.";

        return CommandResult.reply(response);
    }


    @Override
    public CommandResult process(User user, String message) {
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
