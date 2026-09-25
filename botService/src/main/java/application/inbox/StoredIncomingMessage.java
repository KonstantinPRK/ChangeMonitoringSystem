package application.inbox;

import java.time.Instant;
import java.util.UUID;

public record StoredIncomingMessage(
        UUID id,
        String botId,
        long externalId,
        String externalUserId,
        String chatId,
        String text,
        Instant receivedAt,
        int attempts,
        UUID claimToken
) {
}
