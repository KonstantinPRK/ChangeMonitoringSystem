package application.connector.github.inspection.strategy;

import application.connector.github.api.GitHubApiClient;
import application.connector.github.api.GitHubObservationReader;
import application.connector.github.api.GitHubSnapshotReader;
import application.snapshot.ResourceObservation;
import application.connector.github.model.GitHubResourceType;
import application.connector.github.model.GitHubTarget;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class IssueInspector implements GitHubResourceInspector {
    private final GitHubApiClient apiClient;
    private final GitHubObservationReader observationReader;
    private final GitHubSnapshotReader snapshotReader;
    private final GitHubPathEncoder pathEncoder;


    public IssueInspector(
            GitHubApiClient apiClient,
            GitHubObservationReader observationReader,
            GitHubSnapshotReader snapshotReader,
            GitHubPathEncoder pathEncoder
    ) {
        this.apiClient = apiClient;
        this.observationReader = observationReader;
        this.snapshotReader = snapshotReader;
        this.pathEncoder = pathEncoder;
    }


    @Override
    public GitHubResourceType supportedType() {
        return GitHubResourceType.ISSUE;
    }


    @Override
    public CompletionStage<ResourceObservation> inspect(GitHubTarget target, String etag) {
        String path = resourcePath(target, "issues");
        return apiClient.get(path, etag)
                .thenApply(response -> observationReader.read(response, snapshotReader::issue));
    }


    private String resourcePath(GitHubTarget target, String resource) {
        return "/repos/" + pathEncoder.encode(target.owner())
                + '/' + pathEncoder.encode(target.repository())
                + '/' + resource
                + '/' + target.resourceNumber();
    }
}
