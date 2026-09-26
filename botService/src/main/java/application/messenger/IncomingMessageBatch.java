package application.messenger;

import java.util.List;

/**
 * Передаёт между компонентами данные {@code IncomingMessageBatch}.
 */
public record IncomingMessageBatch(String botId, long nextOffset, List<IncomingMessage> messages) {
}
