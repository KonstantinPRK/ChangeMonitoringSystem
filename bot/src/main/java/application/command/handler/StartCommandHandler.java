package application.command.handler;

import application.client.TrackerClient;
import application.command.CommandContext;
import application.command.CommandHandler;
import application.command.UserCommand;

public final class StartCommandHandler implements CommandHandler {
    private final TrackerClient trackerClient;

    public StartCommandHandler(TrackerClient trackerClient) {
        this.trackerClient = trackerClient;
    }

    @Override
    public UserCommand command() {
        return UserCommand.START;
    }

    @Override
    public String handle(CommandContext context) {
        trackerClient.register(context.subscriberId());
        return "Вы зарегистрированы. Откройте справку, чтобы посмотреть доступные команды.";
    }
}
