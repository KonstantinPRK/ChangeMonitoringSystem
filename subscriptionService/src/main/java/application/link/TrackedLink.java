package application.link;

/**
 * Передаёт между компонентами данные {@code TrackedLink}.
 */
public record TrackedLink(long id, Link link, long revision) {
}
