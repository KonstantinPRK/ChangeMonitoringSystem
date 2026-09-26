package application.subscription.operation;

import application.subscription.api.SubscriptionAction;
import application.subscription.api.SubscriptionRequest;
import application.subscription.api.SubscriptionUser;
import application.subscription.api.SubscriptionView;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Формирует запросы внешнего протокола для {@code SubscriptionRequestFactory}.
 */
@Component
public class SubscriptionRequestFactory {
    public SubscriptionRequest create(
            StoredSubscriptionOperation operation,
            SubscriptionAction action
    ) {
        return new SubscriptionRequest(
                operation.id(),
                action,
                user(operation),
                operation.link(),
                operation.tags(),
                operation.filters()
        );
    }


    public List<SubscriptionRequest> createDeletes(
            StoredSubscriptionOperation operation,
            List<SubscriptionView> subscriptions
    ) {
        return subscriptions.stream()
                .limit(100)
                .map(subscription -> deleteRequest(operation, subscription))
                .toList();
    }


    private SubscriptionRequest deleteRequest(
            StoredSubscriptionOperation operation,
            SubscriptionView subscription
    ) {
        String uniqueValue = operation.id() + ":" + subscription.link().address();
        UUID requestId = UUID.nameUUIDFromBytes(uniqueValue.getBytes(StandardCharsets.UTF_8));
        return new SubscriptionRequest(
                requestId,
                SubscriptionAction.DELETE,
                user(operation),
                subscription.link(),
                List.of(),
                List.of()
        );
    }


    private SubscriptionUser user(StoredSubscriptionOperation operation) {
        return new SubscriptionUser(
                operation.user().key().botId(),
                operation.user().key().externalUserId(),
                operation.user().key().chatId()
        );
    }
}
