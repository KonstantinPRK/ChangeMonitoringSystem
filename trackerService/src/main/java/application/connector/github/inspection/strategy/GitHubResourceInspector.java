package application.connector.github.inspection.strategy;

import application.snapshot.ResourceObservation;
import application.connector.github.model.GitHubResourceType;
import application.connector.github.model.GitHubTarget;

import java.util.concurrent.CompletionStage;

public interface GitHubResourceInspector {
    GitHubResourceType supportedType();

    CompletionStage<ResourceObservation> inspect(GitHubTarget target, String etag);
}
