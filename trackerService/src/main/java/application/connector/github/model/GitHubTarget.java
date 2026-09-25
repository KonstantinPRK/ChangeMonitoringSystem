package application.connector.github.model;

public record GitHubTarget(
        GitHubResourceType type,
        String owner,
        String repository,
        Long resourceNumber
) {
}
