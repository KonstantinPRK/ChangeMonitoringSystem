package application.subscription.cache;

import application.subscription.api.SubscriptionServiceApiClient;
import application.subscription.api.SubscriptionView;
import application.user.UserKey;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Реализует ответственность компонента {@code SubscriptionListLoader}.
 */
@Component
public class SubscriptionListLoader {
    private static final int PAGE_SIZE = 500;
    private final SubscriptionServiceApiClient apiClient;
    private final SubscriptionListCache listCache;


    public SubscriptionListLoader(
            SubscriptionServiceApiClient apiClient,
            SubscriptionListCache listCache
    ) {
        this.apiClient = apiClient;
        this.listCache = listCache;
    }


    public CompletionStage<List<SubscriptionView>> load(UserKey user) {
        Optional<List<SubscriptionView>> cachedSubscriptions = listCache.find(user);
        if (cachedSubscriptions.isPresent()) {
            return CompletableFuture.completedFuture(cachedSubscriptions.get());
        }

        List<SubscriptionView> subscriptions = new ArrayList<>();
        return loadPage(user, subscriptions, 0)
                .thenApply(ignored -> save(user, subscriptions));
    }


    public CompletionStage<List<SubscriptionView>> loadFresh(UserKey user) {
        List<SubscriptionView> subscriptions = new ArrayList<>();
        return loadPage(user, subscriptions, 0)
                .thenApply(ignored -> save(user, subscriptions));
    }


    public void invalidate(UserKey user) {
        listCache.invalidate(user);
    }


    private CompletionStage<Void> loadPage(
            UserKey user,
            List<SubscriptionView> subscriptions,
            int offset
    ) {
        return apiClient.list(user, offset, PAGE_SIZE).thenCompose(page -> {
            subscriptions.addAll(page);
            if (page.size() < PAGE_SIZE) return CompletableFuture.completedFuture(null);
            return loadPage(user, subscriptions, offset + PAGE_SIZE);
        });
    }


    private List<SubscriptionView> save(UserKey user, List<SubscriptionView> subscriptions) {
        List<SubscriptionView> result = List.copyOf(subscriptions);
        listCache.save(user, result);
        return result;
    }
}
