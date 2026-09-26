package application.messenger;

import java.time.Instant;

/**
 * Передаёт между компонентами данные {@code IncomingMessage}.
 */
public record IncomingMessage(
        long externalId,
        String externalUserId,
        String chatId,
        String text,
        Instant receivedAt
) {
}
