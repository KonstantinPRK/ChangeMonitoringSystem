package application.config;

import application.client.TrackerClient;
import application.command.CommandCatalog;
import application.command.CommandDispatcher;
import application.command.CommandHandler;
import application.command.handler.HelpCommandHandler;
import application.command.handler.ListCommandHandler;
import application.command.handler.StartCommandHandler;
import application.command.handler.TrackCommandHandler;
import application.command.handler.UntrackCommandHandler;
import application.messenger.MessengerIntegration;
import application.messenger.MessengerAdapter;
import infrastructure.MessengerType;
import application.service.ConversationStateService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BotConfiguration {
    private final ConversationStateService stateService;
    private final Map<MessengerType, MessengerIntegration> integrations;

    public BotConfiguration(TrackerClient trackerClient, Collection<MessengerAdapter> adapters) {
        stateService = new ConversationStateService();

        List<CommandHandler> sharedHandlers = List.of(
            new StartCommandHandler(trackerClient),
            new TrackCommandHandler(stateService),
            new UntrackCommandHandler(stateService),
            new ListCommandHandler(trackerClient)
        );

        Map<MessengerType, MessengerAdapter> adapterByType = adapters.stream()
            .collect(Collectors.toUnmodifiableMap(
                MessengerAdapter::messengerType,
                Function.identity(),
                (first, duplicate) -> {
                    throw new IllegalArgumentException(
                        "Duplicate messenger adapter: " + first.messengerType()
                    );
                }
            ));

        integrations = adapterByType.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> createIntegration(entry.getValue(), sharedHandlers)
            ));
    }

    public MessengerIntegration integration(MessengerType messengerType) {
        MessengerIntegration integration = integrations.get(messengerType);
        if (integration == null) {
            throw new IllegalArgumentException("Messenger is not configured: " + messengerType);
        }
        return integration;
    }

    public ConversationStateService conversationStateService() {
        return stateService;
    }

    private MessengerIntegration createIntegration(
        MessengerAdapter adapter,
        List<CommandHandler> sharedHandlers
    ) {
        CommandCatalog catalog = adapter.commandCatalog();
        List<CommandHandler> handlers = Stream.concat(
            sharedHandlers.stream(),
            Stream.of(new HelpCommandHandler(catalog))
        ).toList();

        return new MessengerIntegration(
            adapter,
            new CommandDispatcher(catalog, handlers)
        );
    }
}
