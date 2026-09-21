package application.messenger;

import application.command.CommandCatalog;
import application.command.CommandContext;
import application.command.CommandDispatcher;
import application.service.ConversationMessageHandler;

public final class MessengerIntegration {
    private final MessengerType messengerType;
    private final CommandCatalog commandCatalog;
    private final CommandDispatcher commandDispatcher;
    private final MessageUpdateSource updateSource;
    private final MessageSender messageSender;

    public MessengerIntegration(
        MessengerAdapter adapter,
        CommandDispatcher commandDispatcher
    ) {
        this.messengerType = adapter.messengerType();
        this.commandCatalog = adapter.commandCatalog();
        this.commandDispatcher = commandDispatcher;
        this.updateSource = adapter.updateSource();
        this.messageSender = adapter.messageSender();
    }

    public CommandCatalog commandCatalog() {
        return commandCatalog;
    }

    /** Accepts a command code already extracted by a platform adapter. */
    public String handleCommand(SubscriberId subscriberId, String commandCode) {
        checkMessenger(subscriberId);

        return commandCatalog.findByCode(commandCode)
            .map(command -> commandDispatcher.dispatch(command, new CommandContext(subscriberId)))
            .orElse("Неизвестная команда. Откройте справку, чтобы посмотреть доступные команды.");
    }

    public void pollOnce(ConversationMessageHandler conversationHandler) {
        updateSource.poll().forEach(message -> process(message, conversationHandler));
    }

    /** Can also be called directly by a future push-based transport. */
    public void process(IncomingMessage message, ConversationMessageHandler conversationHandler) {
        checkMessenger(message.subscriberId());

        String response = message.commandCode()
            .map(code -> handleCommand(message.subscriberId(), code))
            .orElseGet(() -> conversationHandler.handle(message.subscriberId(), message.text()));

        messageSender.send(message.subscriberId(), response);
    }

    private void checkMessenger(SubscriberId subscriberId) {
        if (subscriberId.messengerType() != messengerType) {
            throw new IllegalArgumentException(
                "Subscriber belongs to a different messenger: " + subscriberId.messengerType()
            );
        }
    }
}
