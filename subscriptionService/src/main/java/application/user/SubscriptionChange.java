package application.user;

/**
 * Передаёт между компонентами данные {@code SubscriptionChange}.
 */
public record SubscriptionChange(boolean changed, boolean trackerActionRequired, long revision) {
}
