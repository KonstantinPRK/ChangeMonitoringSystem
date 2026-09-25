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
public class PullRequestInspector implements GitHubResourceInspector {
    private final GitHubApiClient apiClient;
    private final GitHubObservationReader observationReader;
    private final GitHubSnapshotReader snapshotReader;
    private final GitHubPathEncoder pathEncoder;


    public PullRequestInspector(
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
        return GitHubResourceType.PULL_REQUEST;
    }


    @Override
    public CompletionStage<ResourceObservation> inspect(GitHubTarget target, String etag) {
        String path = "/repos/" + pathEncoder.encode(target.owner())
                + '/' + pathEncoder.encode(target.repository())
                + "/pulls/" + target.resourceNumber();
        return apiClient.get(path, etag)
                .thenApply(response -> observationReader.read(response, snapshotReader::pullRequest));
    }
}
