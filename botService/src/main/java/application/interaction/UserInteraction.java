package application.interaction;

import application.user.BotUser;

public record UserInteraction(BotUser user, InteractionResult result) {
}
