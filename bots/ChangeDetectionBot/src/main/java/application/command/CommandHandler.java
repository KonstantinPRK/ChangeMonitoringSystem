package application.command;

import java.util.Map;
import java.util.function.Supplier;

public class CommandHandler {
    Map<CommandType, Supplier<Command>> commands;


    public Command openCommand(CommandType commandType) {
        Supplier<Command> commandFactory = commands.get(commandType);

        if (commandFactory == null) {
            throw new IllegalArgumentException(
                "Unknown command: " + commandType
            );
        }

        return commandFactory.get();
    }
}
