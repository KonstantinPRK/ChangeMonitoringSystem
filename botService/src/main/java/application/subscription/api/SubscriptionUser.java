package application.subscription.api;

/**
 * Передаёт между компонентами данные {@code SubscriptionUser}.
 */
public record SubscriptionUser(String botId, String userId, String chatId) {
}
