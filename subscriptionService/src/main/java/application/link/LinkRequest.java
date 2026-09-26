package application.link;

import application.ActionType;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code LinkRequest}.
 */
public record LinkRequest(UUID requestId, ActionType actionType, Link link, long revision) {
}
