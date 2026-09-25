package application.connector.stackoverflow.inspection;

import application.catalog.model.TrackedResource;
import application.connector.ResourceInspector;
import application.connector.stackoverflow.api.StackOverflowApiClient;
import application.connector.stackoverflow.api.StackOverflowResponseReader;
import application.snapshot.ResourceObservation;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class StackOverflowInspector implements ResourceInspector {
    private final StackOverflowApiClient apiClient;
    private final StackOverflowResponseReader responseReader;


    public StackOverflowInspector(
            StackOverflowApiClient apiClient,
            StackOverflowResponseReader responseReader
    ) {
        this.apiClient = apiClient;
        this.responseReader = responseReader;
    }


    @Override
    public CompletionStage<ResourceObservation> inspect(
            TrackedResource resource,
            String versionToken
    ) {
        long questionId = Long.parseLong(resource.remoteResourceKey());
        return apiClient.question(questionId).thenApply(responseReader::read);
    }
}
