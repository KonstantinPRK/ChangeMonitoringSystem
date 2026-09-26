package application.tracking;

import application.catalog.model.TrackedResource;
import application.connector.ConnectorRouter;
import application.connector.TrackingConnector;
import application.tracking.persistence.ScrapeTaskRepository;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionException;

/**
 * Завершает обработку и фиксирует её результат через {@code ScrapeFailureCompletion}.
 */
@Component
public class ScrapeFailureCompletion {
    private final ScrapeTaskRepository scrapeTaskRepository;
    private final ConnectorRouter connectorRouter;


    public ScrapeFailureCompletion(
            ScrapeTaskRepository scrapeTaskRepository,
            ConnectorRouter connectorRouter
    ) {
        this.scrapeTaskRepository = scrapeTaskRepository;
        this.connectorRouter = connectorRouter;
    }


    public Throwable complete(TrackedResource resource, Throwable failure) {
        Throwable cause = unwrap(failure);
        TrackingConnector connector = connectorRouter.routeProvider(resource.providerKey());
        scrapeTaskRepository.postpone(
                resource,
                connector.failurePolicy().nextAttempt(resource, cause),
                cause.getMessage()
        );
        return cause;
    }


    private Throwable unwrap(Throwable failure) {
        if (failure instanceof CompletionException && failure.getCause() != null) return failure.getCause();
        return failure;
    }
}
