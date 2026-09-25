package application.messenger;

import java.time.Instant;

public record IncomingMessage(
        long externalId,
        String externalUserId,
        String chatId,
        String text,
        Instant receivedAt
) {
}
