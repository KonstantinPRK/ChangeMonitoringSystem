package application.queue;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code QueueMessage}.
 */
public record QueueMessage(long id, UUID messageId, QueueKind kind, String payload, int attempts, UUID claimToken) {
}
