package application.bot;

import application.ActionType;
import application.BotSubscriptionBridge;
import application.config.JsonCodec;
import application.link.Link;
import application.link.LinkParser;
import application.queue.QueueKind;
import application.queue.QueueReceipt;
import application.queue.QueueStore;
import application.subscriptions.SubscriptionRequest;
import application.tracker.TrackerRouter;
import application.user.SubscriptionView;
import application.user.User;
import application.user.UserManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Координирует совместную работу компонентов через {@code BotManager}.
 */
@Service
public class BotManager {
    private final BotRegistry botRegistry;
    private final TrackerRouter trackerRouter;
    private final BotSubscriptionBridge subscriptionBridge;
    private final LinkParser linkParser;
    private final UserManager userManager;
    private final QueueStore queueStore;
    private final JsonCodec jsonCodec;


    public BotManager(
        BotRegistry botRegistry,
        TrackerRouter trackerRouter,
        BotSubscriptionBridge subscriptionBridge,
        LinkParser linkParser,
        UserManager userManager,
        QueueStore queueStore,
        JsonCodec jsonCodec
    ) {
        this.botRegistry = botRegistry;
        this.trackerRouter = trackerRouter;
        this.subscriptionBridge = subscriptionBridge;
        this.linkParser = linkParser;
        this.userManager = userManager;
        this.queueStore = queueStore;
        this.jsonCodec = jsonCodec;
    }


    @Transactional
    public QueueReceipt saveSubscriptionRequest(SubscriptionRequest request) {
        SubscriptionRequest normalizedRequest =
            createRequestWithNormalizedLink(request);
        Optional<QueueReceipt> existingReceipt = queueStore.findExisting(
            QueueKind.SUBSCRIPTION,
            normalizedRequest.requestId(),
            jsonCodec.write(normalizedRequest)
        );

        if (existingReceipt.isPresent()) return existingReceipt.get();

        requireAvailableServices(normalizedRequest);

        return subscriptionBridge.accept(normalizedRequest);
    }


    @Transactional
    public QueueReceipt[] saveSubscriptionRequests(SubscriptionRequest[] requests) {
        QueueReceipt[] receipts = new QueueReceipt[requests.length];

        for (int index = 0; index < requests.length; index++) {
            receipts[index] = saveSubscriptionRequest(requests[index]);
        }

        return receipts;
    }


    public List<SubscriptionView> getSubscriptions(User user, int offset, int limit) {
        botRegistry.requireAvailable(user.botId());

        return userManager.getSubscriptions(user, offset, limit);
    }


    public QueueReceipt requestStatus(UUID requestId) {
        return queueStore.status(QueueKind.SUBSCRIPTION, requestId);
    }


    public List<String> availableSubscriptions() {
        return trackerRouter.supportedHosts();
    }


    private SubscriptionRequest createRequestWithNormalizedLink(
        SubscriptionRequest request
    ) {
        Link normalizedLink = linkParser.normalize(request.link());

        return new SubscriptionRequest(
            request.requestId(),
            request.actionType(),
            request.user(),
            normalizedLink,
            request.tags(),
            request.filters()
        );
    }


    private void requireAvailableServices(SubscriptionRequest request) {
        botRegistry.requireAvailable(request.user().botId());

        if (request.actionType() == ActionType.SAVE) {
            trackerRouter.route(request.link().domain());
        }
    }
}
