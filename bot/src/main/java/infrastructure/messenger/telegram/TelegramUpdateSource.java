package infrastructure.messenger.telegram;

import application.messenger.IncomingMessage;
import application.messenger.MessageUpdateSource;
import application.messenger.MessengerType;
import application.messenger.SubscriberId;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TelegramUpdateSource implements MessageUpdateSource {

    private static final Pattern COMMAND = Pattern.compile(
        "^/([A-Za-z0-9_]+)(?:@([A-Za-z0-9_]+))?(?=\\s|$)"
    );

    private final TelegramApiClient client;
    private final String botUsername;

    public TelegramUpdateSource(TelegramApiClient client, String botUsername) {
        this.client = client;
        this.botUsername = botUsername.startsWith("@")
            ? botUsername.substring(1)
            : botUsername;

        if (this.botUsername.isBlank()) {
            throw new IllegalArgumentException("botUsername must not be blank");
        }
    }

    @Override
    public List<IncomingMessage> poll() {
        return client.getUpdates().stream()
            .filter(message -> !message.text().isBlank())
            .map(this::toIncomingMessage)
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<IncomingMessage> toIncomingMessage(TelegramMessage message) {
        Matcher matcher = COMMAND.matcher(message.text().strip());
        Optional<String> commandCode = Optional.empty();

        if (matcher.find()) {
            String mentionedBot = matcher.group(2);
            if (mentionedBot != null && !botUsername.equalsIgnoreCase(mentionedBot)) {
                return Optional.empty();
            }
            commandCode = Optional.of(matcher.group(1));
        }

        return Optional.of(new IncomingMessage(
            new SubscriberId(MessengerType.TELEGRAM, message.chatId()),
            message.text(),
            commandCode
        ));
    }
}
