package application.user;

/**
 * Передаёт между компонентами данные {@code SubscriptionListVersion}.
 */
public record SubscriptionListVersion(long userId, long revision) {
}
