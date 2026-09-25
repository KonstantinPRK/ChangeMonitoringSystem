package application.notification.transport;

import java.util.UUID;

public record NotificationQueueReceipt(UUID requestId, String status, int attempts, String error) {
}
