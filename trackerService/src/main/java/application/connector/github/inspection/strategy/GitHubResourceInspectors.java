package application.connector.github.inspection.strategy;

import application.connector.github.model.GitHubResourceType;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class GitHubResourceInspectors {
    private final Map<GitHubResourceType, GitHubResourceInspector> inspectors =
            new EnumMap<>(GitHubResourceType.class);


    public GitHubResourceInspectors(List<GitHubResourceInspector> resourceInspectors) {
        resourceInspectors.forEach(inspector -> inspectors.put(inspector.supportedType(), inspector));
    }


    public GitHubResourceInspector get(GitHubResourceType resourceType) {
        return inspectors.get(resourceType);
    }
}
