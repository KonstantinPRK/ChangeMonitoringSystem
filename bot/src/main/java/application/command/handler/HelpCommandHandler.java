package application.command.handler;

import application.command.CommandCatalog;
import application.command.CommandContext;
import application.command.CommandHandler;
import application.command.UserCommand;
import java.util.stream.Collectors;

public final class HelpCommandHandler implements CommandHandler {
    private final CommandCatalog commandCatalog;

    public HelpCommandHandler(CommandCatalog commandCatalog) {
        this.commandCatalog = commandCatalog;
    }

    @Override
    public UserCommand command() {
        return UserCommand.HELP;
    }

    @Override
    public String handle(CommandContext context) {
        return commandCatalog.definitions().stream()
            .map(definition -> definition.displayName() + " — " + definition.description())
            .collect(Collectors.joining("\n"));
    }
}
