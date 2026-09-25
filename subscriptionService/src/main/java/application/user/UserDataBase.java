package application.user;

import application.ActionType;
import application.link.Link;
import application.persistence.LinkEntity;
import application.persistence.LinkRepository;
import application.persistence.SubscriptionEntity;
import application.persistence.SubscriptionRepository;
import application.persistence.UserEntity;
import application.persistence.UserRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDatabase {
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;


    public UserDatabase(
        UserRepository userRepository,
        LinkRepository linkRepository,
        SubscriptionRepository subscriptionRepository
    ) {
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
        this.subscriptionRepository = subscriptionRepository;
    }


    @Transactional
    public SubscriptionChange refreshData(
        ActionType action,
        User user,
        Link link,
        List<String> tags,
        List<String> filters
    ) {
        return switch (action) {
            case SAVE -> saveSubscription(user, link, tags, filters);
            case DELETE -> removeSubscription(user, link);
        };
    }


    private SubscriptionChange saveSubscription(
        User user,
        Link link,
        List<String> tags,
        List<String> filters
    ) {
        LinkEntity storedLink = linkRepository.createAndLock(link);
        UserEntity storedUser = userRepository.createAndLock(user);
        Optional<SubscriptionEntity> existingSubscription =
            subscriptionRepository.find(
                storedUser.getId(),
                storedLink.getId()
            );

        if (existingSubscription.isPresent()) {
            return updateSubscriptionMetadata(
                existingSubscription.get(),
                storedUser,
                storedLink,
                tags,
                filters
            );
        }

        return createSubscription(
            storedUser,
            storedLink,
            tags,
            filters
        );
    }


    private SubscriptionChange updateSubscriptionMetadata(
        SubscriptionEntity subscription,
        UserEntity user,
        LinkEntity link,
        List<String> tags,
        List<String> filters
    ) {
        boolean metadataChanged = subscription.replaceMetadata(tags, filters);

        if (metadataChanged) user.advanceSubscriptionsRevision();

        return new SubscriptionChange(
            metadataChanged,
            false,
            link.getTrackingRevision()
        );
    }


    private SubscriptionChange createSubscription(
        UserEntity user,
        LinkEntity link,
        List<String> tags,
        List<String> filters
    ) {
        boolean firstSubscriber =
            subscriptionRepository.countByLink(link.getId()) == 0;

        subscriptionRepository.save(
            new SubscriptionEntity(user, link, tags, filters)
        );

        user.advanceSubscriptionsRevision();

        if (firstSubscriber) link.advanceTrackingRevision();

        return new SubscriptionChange(
            true,
            firstSubscriber,
            link.getTrackingRevision()
        );
    }


    private SubscriptionChange removeSubscription(User user, Link link) {
        Optional<LinkEntity> storedLink = linkRepository.lock(link);

        if (storedLink.isEmpty()) return unchangedSubscription(0);

        LinkEntity lockedLink = storedLink.get();
        long currentTrackingRevision = lockedLink.getTrackingRevision();
        Optional<UserEntity> storedUser = userRepository.lock(user);

        if (storedUser.isEmpty()) {
            return unchangedSubscription(currentTrackingRevision);
        }

        UserEntity lockedUser = storedUser.get();
        Optional<SubscriptionEntity> storedSubscription =
            subscriptionRepository.find(
                lockedUser.getId(),
                lockedLink.getId()
            );

        if (storedSubscription.isEmpty()) {
            return unchangedSubscription(currentTrackingRevision);
        }

        return deleteStoredSubscription(
            storedSubscription.get(),
            lockedUser,
            lockedLink
        );
    }


    private SubscriptionChange deleteStoredSubscription(
        SubscriptionEntity subscription,
        UserEntity user,
        LinkEntity link
    ) {
        boolean lastSubscriber =
            subscriptionRepository.countByLink(link.getId()) == 1;

        subscriptionRepository.delete(subscription);
        user.advanceSubscriptionsRevision();

        if (lastSubscriber) link.advanceTrackingRevision();

        return new SubscriptionChange(
            true,
            lastSubscriber,
            link.getTrackingRevision()
        );
    }


    private SubscriptionChange unchangedSubscription(long trackingRevision) {
        return new SubscriptionChange(
            false,
            false,
            trackingRevision
        );
    }
}
