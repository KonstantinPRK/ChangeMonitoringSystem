package application.tracking;

import application.catalog.model.TrackedResource;
import application.connector.ConnectorRouter;
import application.connector.TrackingConnector;
import application.snapshot.StoredSnapshot;
import application.tracking.persistence.ScrapeTaskRepository;
import application.snapshot.persistence.SnapshotRepository;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class LinkScrapeProcessor {
    private final ConnectorRouter connectorRouter;
    private final SnapshotRepository snapshotRepository;
    private final ScrapeTaskRepository scrapeTaskRepository;


    public LinkScrapeProcessor(
            ConnectorRouter connectorRouter,
            SnapshotRepository snapshotRepository,
            ScrapeTaskRepository scrapeTaskRepository
    ) {
        this.connectorRouter = connectorRouter;
        this.snapshotRepository = snapshotRepository;
        this.scrapeTaskRepository = scrapeTaskRepository;
    }


    public CompletionStage<ScrapeInspection> inspect(TrackedResource resource) {
        TrackingConnector connector = connectorRouter.routeProvider(resource.providerKey());
        Optional<Instant> blockedUntil = connector.blockedUntil();
        if (blockedUntil.isPresent()) {
            scrapeTaskRepository.defer(resource, blockedUntil.get());
            return CompletableFuture.completedFuture(ScrapeInspection.deferredInspection());
        }

        String versionToken = snapshotRepository.find(resource.id())
                .map(StoredSnapshot::versionToken)
                .orElse(null);
        return connector.resourceInspector().inspect(resource, versionToken)
                .thenApply(ScrapeInspection::observed);
    }
}
