package application.messenger;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code OutgoingMessage}.
 */
public record OutgoingMessage(UUID id, String botId, String chatId, String text) {
}
