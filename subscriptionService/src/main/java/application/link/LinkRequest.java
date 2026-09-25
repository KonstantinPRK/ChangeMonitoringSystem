package application.link;

import application.ActionType;

import java.util.UUID;

public record LinkRequest(UUID requestId, ActionType actionType, Link link, long revision) {
}
