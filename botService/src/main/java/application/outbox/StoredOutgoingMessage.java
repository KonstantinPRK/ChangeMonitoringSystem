package application.outbox;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code StoredOutgoingMessage}.
 */
public record StoredOutgoingMessage(
        UUID id,
        String botId,
        String chatId,
        String text,
        int attempts,
        UUID claimToken
) {
}
