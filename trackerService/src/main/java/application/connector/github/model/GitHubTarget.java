package application.connector.github.model;

/**
 * Передаёт между компонентами данные {@code GitHubTarget}.
 */
public record GitHubTarget(
        GitHubResourceType type,
        String owner,
        String repository,
        Long resourceNumber
) {
}
