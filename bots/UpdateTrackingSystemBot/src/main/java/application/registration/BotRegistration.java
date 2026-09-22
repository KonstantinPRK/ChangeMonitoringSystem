package application.registration;

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
            "UpdateTrackingSystemBot",
            instanceId,
            "UpdateTrackingSystemBot",
            "VK",
            URI.create("http://localhost:8082"),
            1,
            Set.of("RECEIVE_MESSAGES", "SEND_NOTIFICATIONS")
        );
    }
}

