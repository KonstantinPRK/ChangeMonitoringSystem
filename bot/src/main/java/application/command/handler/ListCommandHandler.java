package application.command.handler;

import application.client.TrackerClient;
import application.command.CommandContext;
import application.command.CommandHandler;
import application.command.UserCommand;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public final class ListCommandHandler implements CommandHandler {
    private final TrackerClient trackerClient;

    public ListCommandHandler(TrackerClient trackerClient) {
        this.trackerClient = trackerClient;
    }

    @Override
    public UserCommand command() {
        return UserCommand.LIST;
    }

    @Override
    public String handle(CommandContext context) {
        List<URI> links = trackerClient.listLinks(context.subscriberId());

        if (links.isEmpty()) {
            return "Вы пока не отслеживаете ссылки.";
        }

        return links.stream()
            .map(URI::toString)
            .collect(Collectors.joining("\n", "Отслеживаемые ссылки:\n", ""));
    }
}
