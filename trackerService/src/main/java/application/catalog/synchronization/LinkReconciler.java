package application.catalog.synchronization;

import application.catalog.model.ResolvedResource;
import application.connector.ConnectorRouter;
import application.connector.TrackingConnector;
import application.connector.UnsupportedResourceLinkException;
import application.catalog.persistence.LinkCatalogRepository;
import application.catalog.persistence.RejectedLinkRepository;
import application.catalog.model.TrackedLink;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LinkReconciler {
    private final ConnectorRouter connectorRouter;
    private final LinkCatalogRepository linkCatalog;
    private final RejectedLinkRepository rejectedLinks;


    public LinkReconciler(
            ConnectorRouter connectorRouter,
            LinkCatalogRepository linkCatalog,
            RejectedLinkRepository rejectedLinks
    ) {
        this.connectorRouter = connectorRouter;
        this.linkCatalog = linkCatalog;
        this.rejectedLinks = rejectedLinks;
    }


    public void reconcile(UUID generation, List<TrackedLink> links) {
        for (TrackedLink link : links) reconcile(generation, link);
    }


    private void reconcile(UUID generation, TrackedLink link) {
        try {
            TrackingConnector connector = connectorRouter.routeHost(link.link().domain());
            ResolvedResource resource = connector.linkResolver().resolve(link.link());
            linkCatalog.save(generation, link, resource);
            rejectedLinks.delete(link.id());

        } catch (UnsupportedResourceLinkException exception) {
            rejectedLinks.save(generation, link, exception.getMessage());

        }
    }
}
