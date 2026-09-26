package application.persistence;

import application.user.SubscriptionView;

import java.util.List;

/**
 * Представляет строку подписки вместе с её метаданными.
 */
public class SubscriptionEntity {
    private final long id;
    private final UserEntity user;
    private final LinkEntity link;
    private final List<String> tags;
    private final List<String> filters;


    public SubscriptionEntity(
            long id,
            UserEntity user,
            LinkEntity link,
            List<String> tags,
            List<String> filters
    ) {
        this.id = id;
        this.user = user;
        this.link = link;
        this.tags = List.copyOf(tags);
        this.filters = List.copyOf(filters);
    }


    public long getId() {
        return id;
    }


    public boolean hasMetadata(List<String> tags, List<String> filters) {
        return this.tags.equals(tags) && this.filters.equals(filters);
    }


    public SubscriptionView toView() {
        return new SubscriptionView(link.toLink(), tags, filters);
    }
}
