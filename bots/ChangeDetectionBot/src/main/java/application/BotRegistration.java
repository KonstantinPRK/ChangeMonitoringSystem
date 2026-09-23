package application;

import java.net.URI;
import java.util.Set;

public record BotRegistration(
    String botId,
    String instanceId,
    String botName,
    String communicationChannel,
    URI baseUrl,
    int contractVersion,
    Set<String> capabilities
) {
    public static BotRegistration local(String instanceId) {
        return new BotRegistration(
            "ChangeDetectionBot",
            instanceId,
            "ChangeDetectionBot",
            "TELEGRAM",
            URI.create("http://localhost:8081"),
            1,
            Set.of("RECEIVE_MESSAGES", "SEND_NOTIFICATIONS")
        );
    }
}

