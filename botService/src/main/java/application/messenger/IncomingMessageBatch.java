package application.messenger;

import java.util.List;

public record IncomingMessageBatch(String botId, long nextOffset, List<IncomingMessage> messages) {
}
