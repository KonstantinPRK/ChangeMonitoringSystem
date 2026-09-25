package application.connector.stackoverflow;

import application.connector.ConnectorDescriptor;
import application.connector.LinkResolver;
import application.connector.ProviderFailurePolicy;
import application.connector.ResourceInspector;
import application.connector.TrackingConnector;
import application.connector.stackoverflow.failure.StackOverflowFailurePolicy;
import application.connector.stackoverflow.inspection.StackOverflowInspector;
import application.connector.stackoverflow.link.StackOverflowLinkResolver;
import application.connector.stackoverflow.quota.StackOverflowQuotaGate;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Component
public class StackOverflowConnector implements TrackingConnector {
    private final ConnectorDescriptor descriptor;
    private final StackOverflowLinkResolver linkResolver;
    private final StackOverflowInspector resourceInspector;
    private final StackOverflowFailurePolicy failurePolicy;
    private final StackOverflowQuotaGate quotaGate;


    public StackOverflowConnector(
            StackOverflowProperties properties,
            StackOverflowLinkResolver linkResolver,
            StackOverflowInspector resourceInspector,
            StackOverflowFailurePolicy failurePolicy,
            StackOverflowQuotaGate quotaGate
    ) {
        descriptor = new ConnectorDescriptor(
                "stackoverflow",
                properties.supportedHosts(),
                Set.of("question"),
                properties.scrapeInterval()
        );
        this.linkResolver = linkResolver;
        this.resourceInspector = resourceInspector;
        this.failurePolicy = failurePolicy;
        this.quotaGate = quotaGate;
    }


    @Override
    public ConnectorDescriptor descriptor() {
        return descriptor;
    }


    @Override
    public LinkResolver linkResolver() {
        return linkResolver;
    }


    @Override
    public ResourceInspector resourceInspector() {
        return resourceInspector;
    }


    @Override
    public ProviderFailurePolicy failurePolicy() {
        return failurePolicy;
    }


    @Override
    public Optional<Instant> blockedUntil() {
        return quotaGate.blockedUntil();
    }
}
