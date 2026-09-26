package application.user;

import java.util.List;

/**
 * Передаёт между компонентами данные {@code SubscriberBatch}.
 */
public record SubscriberBatch(List<User> users, long lastUserId) {
}
