package application.queue;

import java.util.UUID;

public record QueueMessage(long id, UUID messageId, QueueKind kind, String payload, int attempts, UUID claimToken) {
}
