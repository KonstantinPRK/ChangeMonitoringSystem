package application.bot;

import java.net.URI;
import java.util.Set;

public record BotDescriptor(
    String botId,
    String instanceId,
    String botName,
    String communicationChannel,
    URI baseUrl,
    int contractVersion,
    Set<String> capabilities
) {
}

