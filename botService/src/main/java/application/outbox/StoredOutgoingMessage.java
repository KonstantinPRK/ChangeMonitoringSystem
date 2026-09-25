package application.outbox;

import java.util.UUID;

public record StoredOutgoingMessage(
        UUID id,
        String botId,
        String chatId,
        String text,
        int attempts,
        UUID claimToken
) {
}
