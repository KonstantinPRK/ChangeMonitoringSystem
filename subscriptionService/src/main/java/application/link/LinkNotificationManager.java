package application.link;

import application.TrackerSubscriptionBridge;
import application.queue.QueueReceipt;
import application.tracker.TrackerRouter;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkNotificationManager {
    private final LinkParser linkParser;
    private final TrackerRouter trackerRouter;
    private final TrackerSubscriptionBridge subscriptionBridge;
    private final Validator validator;


    public LinkNotificationManager(
        LinkParser linkParser,
        TrackerRouter trackerRouter,
        TrackerSubscriptionBridge subscriptionBridge,
        Validator validator
    ) {
        this.linkParser = linkParser;
        this.trackerRouter = trackerRouter;
        this.subscriptionBridge = subscriptionBridge;
        this.validator = validator;
    }


    @Transactional
    public QueueReceipt saveLinkNotification(LinkNotification notification) {
        validateNotification(notification);

        LinkNotification normalizedNotification =
            createNotificationWithNormalizedLink(notification);

        trackerRouter.requireOwnership(
            normalizedNotification.trackerId(),
            normalizedNotification.link().domain()
        );

        return subscriptionBridge.accept(normalizedNotification);
    }


    @Transactional
    public void saveLinkNotifications(LinkNotification[] notifications) {
        for (LinkNotification notification : notifications) {
            saveLinkNotification(notification);
        }
    }


    private void validateNotification(LinkNotification notification) {
        var violations = validator.validate(notification);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }


    private LinkNotification createNotificationWithNormalizedLink(
        LinkNotification notification
    ) {
        Link normalizedLink = linkParser.normalize(notification.link());

        return new LinkNotification(
            notification.eventId(),
            notification.trackerId(),
            normalizedLink,
            notification.message(),
            notification.occurredAt()
        );
    }
}
