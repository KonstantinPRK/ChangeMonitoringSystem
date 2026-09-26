package application.subscription.api;

import application.subscription.model.Link;

import java.util.List;

/**
 * Передаёт между компонентами данные {@code SubscriptionView}.
 */
public record SubscriptionView(Link link, List<String> tags, List<String> filters) {
}
