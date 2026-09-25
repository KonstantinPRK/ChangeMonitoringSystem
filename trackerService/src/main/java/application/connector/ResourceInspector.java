package application.connector;

import application.catalog.model.TrackedResource;
import application.snapshot.ResourceObservation;

import java.util.concurrent.CompletionStage;

public interface ResourceInspector {
    CompletionStage<ResourceObservation> inspect(TrackedResource resource, String versionToken);
}
