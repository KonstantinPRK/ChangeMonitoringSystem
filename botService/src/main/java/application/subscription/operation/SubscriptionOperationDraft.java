package application.subscription.operation;

import application.subscription.model.Link;

import java.util.List;

public record SubscriptionOperationDraft(
        SubscriptionOperationType type,
        Link link,
        List<String> tags,
        List<String> filters
) {
    public static SubscriptionOperationDraft simple(SubscriptionOperationType type) {
        return new SubscriptionOperationDraft(type, null, List.of(), List.of());
    }
}
