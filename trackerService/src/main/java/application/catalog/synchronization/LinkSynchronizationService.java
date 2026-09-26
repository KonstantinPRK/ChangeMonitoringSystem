package application.catalog.synchronization;

import application.config.SubscriptionServiceProperties;
import application.config.TrackerProperties;
import application.catalog.persistence.LinkSynchronizationRepository;
import application.catalog.api.SubscriptionLinkApiClient;
import application.catalog.model.TrackedLink;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Реализует прикладной сценарий {@code LinkSynchronizationService}.
 */
@Component
public class LinkSynchronizationService {
    private static final int PAGE_SIZE = 500;
    private final SubscriptionLinkApiClient linkApiClient;
    private final LinkReconciler linkReconciler;
    private final LinkSynchronizationCompletion synchronizationCompletion;
    private final LinkSynchronizationRepository synchronizationRepository;
    private final TrackerProperties trackerProperties;
    private final SubscriptionServiceProperties serviceProperties;


    public LinkSynchronizationService(
            SubscriptionLinkApiClient linkApiClient,
            LinkReconciler linkReconciler,
            LinkSynchronizationCompletion synchronizationCompletion,
            LinkSynchronizationRepository synchronizationRepository,
            TrackerProperties trackerProperties,
            SubscriptionServiceProperties serviceProperties
    ) {
        this.linkApiClient = linkApiClient;
        this.linkReconciler = linkReconciler;
        this.synchronizationCompletion = synchronizationCompletion;
        this.synchronizationRepository = synchronizationRepository;
        this.trackerProperties = trackerProperties;
        this.serviceProperties = serviceProperties;
    }


    public CompletionStage<Void> synchronize() {
        return synchronizationRepository.start(
                trackerProperties.id(),
                serviceProperties.synchronizationLease()
        ).map(this::synchronize).orElseGet(() -> CompletableFuture.completedFuture(null));
    }


    private CompletionStage<Void> synchronize(UUID generation) {
        return loadPage(generation, 0L)
                .thenRun(() -> synchronizationCompletion.complete(generation))
                .whenComplete((ignored, failure) -> completeFailure(generation, failure));
    }


    private CompletionStage<Void> loadPage(UUID generation, long afterId) {
        return linkApiClient.load(afterId, PAGE_SIZE).thenCompose(links -> {
            linkReconciler.reconcile(generation, links);
            if (links.isEmpty()) return CompletableFuture.completedFuture(null);

            long nextAfterId = links.get(links.size() - 1).id();
            synchronizationRepository.saveProgress(
                    trackerProperties.id(),
                    generation,
                    nextAfterId,
                    serviceProperties.synchronizationLease()
            );
            if (links.size() < PAGE_SIZE) return CompletableFuture.completedFuture(null);
            return loadPage(generation, nextAfterId);
        });
    }


    private void completeFailure(UUID generation, Throwable failure) {
        if (failure == null) return;
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        synchronizationCompletion.fail(generation, cause);
    }
}
