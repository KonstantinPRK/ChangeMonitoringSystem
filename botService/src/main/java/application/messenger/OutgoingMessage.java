package application.messenger;

import java.util.UUID;

public record OutgoingMessage(UUID id, String botId, String chatId, String text) {
}
