package application.tracking;

import application.catalog.model.TrackedResource;
import application.connector.ConnectorRouter;
import application.connector.TrackingConnector;
import application.snapshot.ChangeDetector;
import application.snapshot.ResourceObservation;
import application.snapshot.ResourceSnapshot;
import application.snapshot.StoredSnapshot;
import application.notification.outbox.NotificationOutbox;
import application.tracking.persistence.ScrapeTaskRepository;
import application.snapshot.persistence.SnapshotRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

@Component
public class ScrapeCompletion {
    private final SnapshotRepository snapshotRepository;
    private final ScrapeTaskRepository scrapeTaskRepository;
    private final NotificationOutbox notificationOutbox;
    private final ChangeDetector changeDetector;
    private final ConnectorRouter connectorRouter;
    private final Clock clock;


    public ScrapeCompletion(
            SnapshotRepository snapshotRepository,
            ScrapeTaskRepository scrapeTaskRepository,
            NotificationOutbox notificationOutbox,
            ChangeDetector changeDetector,
            ConnectorRouter connectorRouter,
            Clock clock
    ) {
        this.snapshotRepository = snapshotRepository;
        this.scrapeTaskRepository = scrapeTaskRepository;
        this.notificationOutbox = notificationOutbox;
        this.changeDetector = changeDetector;
        this.connectorRouter = connectorRouter;
        this.clock = clock;
    }


    @Transactional
    public boolean complete(TrackedResource resource, ScrapeInspection inspection) {
        if (inspection.deferred()) return false;

        ResourceObservation observation = inspection.observation();
        boolean changed = !observation.notModified() && saveObservation(resource, observation);
        TrackingConnector connector = connectorRouter.routeProvider(resource.providerKey());
        scrapeTaskRepository.complete(
                resource,
                clock.instant().plus(connector.descriptor().scrapeInterval())
        );
        return changed;
    }


    private boolean saveObservation(TrackedResource resource, ResourceObservation observation) {
        ResourceSnapshot current = observation.snapshot();
        Optional<StoredSnapshot> previous = snapshotRepository.find(resource.id());
        boolean changed = previous.isPresent() && changeDetector.changed(previous.get(), current);
        if (changed) {
            notificationOutbox.enqueue(resource, current);
        }
        snapshotRepository.save(resource.id(), observation.versionToken(), current);
        return changed;
    }
}
