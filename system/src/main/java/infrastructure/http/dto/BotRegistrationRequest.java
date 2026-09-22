package infrastructure.http.dto;

import java.net.URI;
import java.util.Set;

public record BotRegistrationRequest(
    String botId,
    String botName,
    String communicationChannel,
    URI baseUrl,
    int contractVersion,
    Set<String> capabilities
) {
}
