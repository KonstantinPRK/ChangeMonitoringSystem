package application.user;

import application.ActionType;
import application.cache.SubscriptionCache;
import application.link.Link;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserManager {
    private final UserDatabase userDatabase;
    private final UserToLinksDatabase userSubscriptions;
    private final LinkToUsersDatabase linkSubscribers;
    private final SubscriptionCache subscriptionCache;


    public UserManager(
        UserDatabase userDatabase,
        UserToLinksDatabase userSubscriptions,
        LinkToUsersDatabase linkSubscribers,
        SubscriptionCache subscriptionCache
    ) {
        this.userDatabase = userDatabase;
        this.userSubscriptions = userSubscriptions;
        this.linkSubscribers = linkSubscribers;
        this.subscriptionCache = subscriptionCache;
    }


    @Transactional
    public SubscriptionChange refreshData(
        ActionType action,
        User user,
        Link link,
        List<String> tags,
        List<String> filters
    ) {
        return userDatabase.refreshData(action, user, link, tags, filters);
    }


    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<SubscriptionView> getSubscriptions(User user, int offset, int limit) {
        Optional<SubscriptionListVersion> version =
            userSubscriptions.version(user);

        if (version.isEmpty()) return List.of();

        SubscriptionListVersion current = version.get();

        return getOrLoadSubscriptions(
            current,
            offset,
            limit
        );
    }


    @Transactional(readOnly = true)
    public SubscriberBatch getLinkSubscribers(Link link, long afterUserId, int limit) {
        return linkSubscribers.read(link, afterUserId, limit);
    }


    private List<SubscriptionView> getOrLoadSubscriptions(
        SubscriptionListVersion version,
        int offset,
        int limit
    ) {
        String cacheKey = createSubscriptionsCacheKey(
            version,
            offset,
            limit
        );

        Optional<List<SubscriptionView>> cachedSubscriptions =
            subscriptionCache.get(cacheKey);

        if (cachedSubscriptions.isPresent()) {
            return cachedSubscriptions.get();
        }

        List<SubscriptionView> subscriptionsPage = userSubscriptions.read(
            version.userId(),
            offset,
            limit
        );

        subscriptionCache.put(cacheKey, subscriptionsPage);

        return subscriptionsPage;
    }


    private String createSubscriptionsCacheKey(
        SubscriptionListVersion version,
        int offset,
        int limit
    ) {
        return "subscriptions:"
            + version.userId()
            + ":"
            + version.revision()
            + ":"
            + offset
            + ":"
            + limit;
    }
}
