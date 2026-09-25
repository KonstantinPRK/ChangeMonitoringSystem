package application.subscription.api;

import java.net.URI;

public record BotRegistrationRequest(String botId, URI baseUrl) {
}
