package application.notification.creation;

import application.config.TrackerProperties;
import application.notification.model.LinkNotification;
import application.notification.model.NotificationEvent;
import application.snapshot.ResourceSnapshot;
import application.catalog.model.Link;
import application.catalog.model.TrackedResource;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.UUID;

@Component
public class NotificationEventFactory {
    private final TrackerProperties trackerProperties;
    private final NotificationMessageFactory messageFactory;
    private final ObjectMapper objectMapper;
    private final Clock clock;


    public NotificationEventFactory(
            TrackerProperties trackerProperties,
            NotificationMessageFactory messageFactory,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.trackerProperties = trackerProperties;
        this.messageFactory = messageFactory;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }


    public NotificationEvent create(TrackedResource resource, ResourceSnapshot snapshot) {
        UUID eventId = UUID.randomUUID();
        LinkNotification notification = new LinkNotification(
                eventId,
                trackerProperties.id(),
                new Link(resource.domain(), resource.address()),
                messageFactory.create(resource, snapshot),
                clock.instant()
        );
        return new NotificationEvent(eventId, objectMapper.writeValueAsString(notification));
    }
}
