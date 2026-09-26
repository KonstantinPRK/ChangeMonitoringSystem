package application.connector.github;

import application.connector.ConnectorDescriptor;
import application.connector.LinkResolver;
import application.connector.ProviderFailurePolicy;
import application.connector.ResourceInspector;
import application.connector.TrackingConnector;
import application.connector.github.failure.GitHubFailurePolicy;
import application.connector.github.inspection.GitHubInspector;
import application.connector.github.link.GitHubLinkResolver;
import application.connector.github.ratelimit.GitHubRateLimitGate;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Реализует ответственность компонента {@code GitHubConnector}.
 */
@Component
public class GitHubConnector implements TrackingConnector {
    private final ConnectorDescriptor descriptor;
    private final GitHubLinkResolver linkResolver;
    private final GitHubInspector resourceInspector;
    private final GitHubFailurePolicy failurePolicy;
    private final GitHubRateLimitGate rateLimitGate;


    public GitHubConnector(
            GitHubProperties properties,
            GitHubLinkResolver linkResolver,
            GitHubInspector resourceInspector,
            GitHubFailurePolicy failurePolicy,
            GitHubRateLimitGate rateLimitGate
    ) {
        descriptor = new ConnectorDescriptor(
                "github",
                properties.supportedHosts(),
                Set.of("repository", "issue", "pull_request"),
                properties.scrapeInterval()
        );
        this.linkResolver = linkResolver;
        this.resourceInspector = resourceInspector;
        this.failurePolicy = failurePolicy;
        this.rateLimitGate = rateLimitGate;
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
        return rateLimitGate.blockedUntil();
    }
}
