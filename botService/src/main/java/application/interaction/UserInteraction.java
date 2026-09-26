package application.interaction;

import application.user.BotUser;

/**
 * Передаёт между компонентами данные {@code UserInteraction}.
 */
public record UserInteraction(BotUser user, InteractionResult result) {
}
