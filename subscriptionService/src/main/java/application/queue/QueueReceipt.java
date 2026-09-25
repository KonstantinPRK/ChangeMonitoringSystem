package application.queue;

import java.util.UUID;

public record QueueReceipt(UUID requestId, String status, int attempts, String error) {
}
