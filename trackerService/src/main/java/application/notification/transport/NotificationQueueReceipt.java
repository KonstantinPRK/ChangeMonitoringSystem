package application.notification.transport;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code NotificationQueueReceipt}.
 */
public record NotificationQueueReceipt(UUID requestId, String status, int attempts, String error) {
}
