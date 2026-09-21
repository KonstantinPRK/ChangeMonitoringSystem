package application.command;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CommandDispatcher {

    private final Map<UserCommand, CommandHandler> handlerByCommand;

    public CommandDispatcher(
        CommandCatalog catalog,
        Collection<CommandHandler> handlers
    ) {
        this.handlerByCommand = handlers.stream()
            .collect(Collectors.toUnmodifiableMap(
                CommandHandler::command,
                Function.identity(),
                (first, duplicate) -> {
                    throw new IllegalArgumentException(
                        "Duplicate handler for command: " + first.command()
                    );
                }
            ));

        catalog.definitions().stream()
            .map(CommandDefinition::command)
            .filter(command -> !handlerByCommand.containsKey(command))
            .findFirst()
            .ifPresent(command -> {
                throw new IllegalArgumentException(
                    "Handler is not registered for catalog command: " + command
                );
            });
    }

    public String dispatch(UserCommand command, CommandContext context) {
        CommandHandler handler = handlerByCommand.get(command);

        if (handler == null) {
            throw new IllegalStateException(
                "Handler is not registered for command: " + command
            );
        }

        return handler.handle(context);
    }
}
