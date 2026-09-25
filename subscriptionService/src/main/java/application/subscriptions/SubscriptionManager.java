package application.subscriptions;

import application.link.LinkRequestFactory;
import application.link.LinkRequestQueue;
import application.metrics.ServiceMetrics;
import application.queue.QueueReceipt;
import application.user.SubscriptionChange;
import application.user.UserManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionManager {
    private final UserManager userManager;
    private final SubscriptionRequestQueue subscriptionRequestQueue;
    private final LinkRequestFactory linkRequestFactory;
    private final LinkRequestQueue trackerRequestQueue;
    private final ServiceMetrics metrics;


    public SubscriptionManager(
        UserManager userManager,
        SubscriptionRequestQueue subscriptionRequestQueue,
        LinkRequestFactory linkRequestFactory,
        LinkRequestQueue trackerRequestQueue,
        ServiceMetrics metrics
    ) {
        this.userManager = userManager;
        this.subscriptionRequestQueue = subscriptionRequestQueue;
        this.linkRequestFactory = linkRequestFactory;
        this.trackerRequestQueue = trackerRequestQueue;
        this.metrics = metrics;
    }


    public QueueReceipt saveSubscriptionRequest(SubscriptionRequest request) {
        QueueReceipt receipt = subscriptionRequestQueue.addToQueue(request);

        metrics.acceptedSubscription();

        return receipt;
    }


    @Transactional
    public void process(SubscriptionRequest request) {
        SubscriptionChange change = userManager.refreshData(
            request.actionType(),
            request.user(),
            request.link(),
            request.tags(),
            request.filters()
        );

        enqueueTrackerRequestIfRequired(request, change);
        metrics.subscriptionProcessed(
            request.actionType().name(),
            change.changed()
        );
    }


    private void enqueueTrackerRequestIfRequired(
        SubscriptionRequest request,
        SubscriptionChange change
    ) {
        if (!change.trackerActionRequired()) return;

        trackerRequestQueue.addToQueue(
            linkRequestFactory.create(request, change.revision())
        );
    }
}
