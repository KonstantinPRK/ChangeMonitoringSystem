package application.subscription.api;

import java.net.URI;

/**
 * Передаёт между компонентами данные {@code BotRegistrationRequest}.
 */
public record BotRegistrationRequest(String botId, URI baseUrl) {
}
