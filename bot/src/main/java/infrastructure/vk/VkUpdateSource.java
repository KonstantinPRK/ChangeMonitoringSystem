package infrastructure.vk;

import application.command.CommandCatalog;
import application.messenger.IncomingMessage;
import application.messenger.MessageUpdateSource;
import infrastructure.MessengerType;
import application.messenger.SubscriberId;
import java.util.List;
import java.util.Optional;

public final class VkUpdateSource implements MessageUpdateSource {

    private final VkApiClient apiClient;
    private final CommandCatalog commandCatalog;

    public VkUpdateSource(VkApiClient apiClient, CommandCatalog commandCatalog) {
        this.apiClient = apiClient;
        this.commandCatalog = commandCatalog;
    }

    @Override
    public List<IncomingMessage> poll() {
        return apiClient.getUpdates().stream()
            .filter(message -> !message.text().isBlank())
            .map(message -> new IncomingMessage(
                new SubscriberId(MessengerType.VK, message.peerId()),
                message.text(),
                commandCode(message.text())
            ))
            .toList();
    }

    private Optional<String> commandCode(String text) {
        String stripped = text.strip();

        if (stripped.startsWith("/")) {
            String firstToken = stripped.split("\\s+", 2)[0];
            if (firstToken.length() > 1) {
                return Optional.of(firstToken.substring(1));
            }
            return Optional.empty();
        }

        return commandCatalog.findByCode(stripped)
            .map(command -> command.code());
    }
}
