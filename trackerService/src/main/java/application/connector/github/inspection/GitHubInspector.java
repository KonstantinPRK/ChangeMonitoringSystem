package application.connector.github.inspection;

import application.catalog.model.TrackedResource;
import application.connector.ResourceInspector;
import application.connector.github.inspection.strategy.GitHubResourceInspector;
import application.connector.github.inspection.strategy.GitHubResourceInspectors;
import application.connector.github.link.GitHubLinkParser;
import application.connector.github.model.GitHubTarget;
import application.snapshot.ResourceObservation;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class GitHubInspector implements ResourceInspector {
    private final GitHubLinkParser linkParser;
    private final GitHubResourceInspectors resourceInspectors;


    public GitHubInspector(
            GitHubLinkParser linkParser,
            GitHubResourceInspectors resourceInspectors
    ) {
        this.linkParser = linkParser;
        this.resourceInspectors = resourceInspectors;
    }


    @Override
    public CompletionStage<ResourceObservation> inspect(
            TrackedResource resource,
            String versionToken
    ) {
        GitHubTarget target = linkParser.parse(resource.address());
        GitHubResourceInspector inspector = resourceInspectors.get(target.type());
        return inspector.inspect(target, versionToken);
    }
}
