package application.user;

import application.link.Link;

import java.util.List;

public record SubscriptionView(Link link, List<String> tags, List<String> filters) {
    public SubscriptionView {
        tags = List.copyOf(tags);
        filters = List.copyOf(filters);
    }
}
