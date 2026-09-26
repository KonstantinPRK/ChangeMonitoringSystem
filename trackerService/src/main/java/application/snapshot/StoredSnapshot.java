package application.snapshot;

/**
 * Передаёт между компонентами данные {@code StoredSnapshot}.
 */
public record StoredSnapshot(String versionToken, ResourceSnapshot snapshot) {
}
