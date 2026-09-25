package application.subscription.operation;

import application.subscription.model.Link;
import application.user.BotUser;

import java.util.List;
import java.util.UUID;

public record StoredSubscriptionOperation(
        UUID id,
        SubscriptionOperationType type,
        BotUser user,
        Link link,
        List<String> tags,
        List<String> filters,
        int attempts,
        UUID claimToken
) {
}
