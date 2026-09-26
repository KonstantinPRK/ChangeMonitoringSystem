package application.queue;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code QueueReceipt}.
 */
public record QueueReceipt(UUID requestId, String status, int attempts, String error) {
}
