package application.subscription.api;

import application.subscription.model.Link;

import java.util.List;
import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code SubscriptionRequest}.
 */
public record SubscriptionRequest(
        UUID requestId,
        SubscriptionAction actionType,
        SubscriptionUser user,
        Link link,
        List<String> tags,
        List<String> filters
) {
}
