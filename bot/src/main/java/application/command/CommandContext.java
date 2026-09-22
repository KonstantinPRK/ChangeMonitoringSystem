package application.command;

import application.messenger.SubscriberId;

public record CommandContext(SubscriberId subscriberId) {
}
