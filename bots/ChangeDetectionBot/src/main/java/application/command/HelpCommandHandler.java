package application.command;

import application.interaction.ConversationState;
import application.user.User;

public class HelpCommandHandler implements Command {
    @Override
    public CommandResult start(User user) {
        StringBuilder response =
            new StringBuilder("Доступные команды:\n");

        for (CommandType availableCommand : CommandType.values()) {
            response
                .append('/')
                .append(availableCommand.code())
                .append(availableCommand.description())
                .append('\n');
        }

        return CommandResult.reply(response.toString());
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
