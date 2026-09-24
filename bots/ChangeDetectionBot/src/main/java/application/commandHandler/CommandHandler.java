package application.commandHandler;

import application.ConversationState;
import application.User;

import java.util.EnumMap;
import java.util.Map;

public class CommandHandler {
    private final Map<CommandType, Command> commands;


    public CommandHandler() {
        commands = new EnumMap<>(CommandType.class);

        commands.put(CommandType.START, new StartCommandHandler());
        commands.put(CommandType.HELP, new HelpCommandHandler());
        commands.put(CommandType.TRACK, new TrackCommandHandler());
        commands.put(CommandType.UNTRACK, new UntrackCommandHandler());
        commands.put(CommandType.LIST, new ListCommandHandler());
        commands.put(CommandType.STOP, new StopCommandHandler());
        commands.put(CommandType.DELETE, new DeleteCommandHandler());
    }


    public String process(CommandType commandType, User user) {
        return getCommand(commandType).process(commandType, user);
    }


    public ConversationState getConversationState(CommandType commandType) {
        return getCommand(commandType).getConversationState();
    }


    private Command getCommand(
        CommandType commandType
    ) {
        Command command = commands.get(commandType);

        if (command == null) {
            throw new IllegalArgumentException(
                "Unsupported command: " + commandType
            );
        }

        return command;
    }
}
