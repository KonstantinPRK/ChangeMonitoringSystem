package application.catalog.model;

/**
 * Передаёт между компонентами данные {@code TrackedLink}.
 */
public record TrackedLink(long id, Link link, long revision) {
}
