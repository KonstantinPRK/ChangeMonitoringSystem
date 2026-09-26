package application.subscription.api;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code SubscriptionQueueReceipt}.
 */
public record SubscriptionQueueReceipt(UUID requestId, String status, int attempts, String error) {
    public boolean completed() {
        return "COMPLETED".equals(status);
    }


    public boolean failed() {
        return "FAILED".equals(status);
    }
}
