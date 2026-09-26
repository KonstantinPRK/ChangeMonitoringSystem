package application.catalog.synchronization;

import application.config.TrackerProperties;
import application.catalog.persistence.LinkCatalogRepository;
import application.catalog.persistence.LinkSynchronizationRepository;
import application.catalog.persistence.RejectedLinkRepository;
import application.transaction.AfterCommitAction;
import application.work.ScrapeWorkSignal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Завершает обработку и фиксирует её результат через {@code LinkSynchronizationCompletion}.
 */
@Component
public class LinkSynchronizationCompletion {
    private final TrackerProperties trackerProperties;
    private final LinkCatalogRepository linkCatalog;
    private final RejectedLinkRepository rejectedLinks;
    private final LinkSynchronizationRepository synchronizationRepository;
    private final ScrapeWorkSignal scrapeWorkSignal;
    private final AfterCommitAction afterCommitAction;


    public LinkSynchronizationCompletion(
            TrackerProperties trackerProperties,
            LinkCatalogRepository linkCatalog,
            RejectedLinkRepository rejectedLinks,
            LinkSynchronizationRepository synchronizationRepository,
            ScrapeWorkSignal scrapeWorkSignal,
            AfterCommitAction afterCommitAction
    ) {
        this.trackerProperties = trackerProperties;
        this.linkCatalog = linkCatalog;
        this.rejectedLinks = rejectedLinks;
        this.synchronizationRepository = synchronizationRepository;
        this.scrapeWorkSignal = scrapeWorkSignal;
        this.afterCommitAction = afterCommitAction;
    }


    @Transactional
    public void complete(UUID generation) {
        if (!synchronizationRepository.lockOwnedGeneration(trackerProperties.id(), generation)) return;
        linkCatalog.deactivateMissing(generation);
        rejectedLinks.deleteMissing(generation);
        synchronizationRepository.complete(trackerProperties.id(), generation);
        afterCommitAction.execute(scrapeWorkSignal::signal);
    }


    public void fail(UUID generation, Throwable failure) {
        synchronizationRepository.fail(
                trackerProperties.id(),
                generation,
                failure.getMessage()
        );
    }
}
