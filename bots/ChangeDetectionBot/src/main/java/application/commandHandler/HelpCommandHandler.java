package application.commandHandler;

import application.ConversationState;
import application.User;

public class HelpCommandHandler implements Command {

    @Override
    public String start(User user) {
        StringBuilder response =
            new StringBuilder("Доступные команды:\n");

        for (CommandType availableCommand : CommandType.values()) {
            response
                .append('/')
                .append(availableCommand.code())
                .append(availableCommand.description())
                .append('\n');
        }

        return response.toString();
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
