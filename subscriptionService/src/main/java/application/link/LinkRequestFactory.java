package application.link;

import application.subscriptions.SubscriptionRequest;

import org.springframework.stereotype.Component;

/**
 * Формирует запросы внешнего протокола для {@code LinkRequestFactory}.
 */
@Component
public class LinkRequestFactory {


    public LinkRequest create(SubscriptionRequest request, long revision) {
        return new LinkRequest(request.requestId(), request.actionType(), request.link(), revision);
    }
}
