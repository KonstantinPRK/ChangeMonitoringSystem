package application.bot;

import application.registration.RemoteSystemStatus;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

public record BotInstance(
    String botId,
    String botName,
    String communicationChannel,
    URI baseUrl,
    int contractVersion,
    Set<String> capabilities,
    RemoteSystemStatus status,
    Instant availabilityConfirmedUntil
) {
    public BotInstance {
        botId = requireText(botId, "botId");
        botName = requireText(botName, "botName");
        communicationChannel = requireText(communicationChannel, "communicationChannel");
        baseUrl = requireAbsoluteUri(baseUrl, "baseUrl");

        if (contractVersion < 1) {
            throw new IllegalArgumentException("contractVersion must be positive");
        }

        capabilities = Set.copyOf(capabilities);
    }


    public BotInstance confirmAvailability(Instant confirmedUntil) {
        return new BotInstance(
            botId,
            botName,
            communicationChannel,
            baseUrl,
            contractVersion,
            capabilities,
            RemoteSystemStatus.ACTIVE,
            confirmedUntil
        );
    }


    public BotInstance markUnavailable() {
        return new BotInstance(
            botId,
            botName,
            communicationChannel,
            baseUrl,
            contractVersion,
            capabilities,
            RemoteSystemStatus.UNAVAILABLE,
            availabilityConfirmedUntil
        );
    }


    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.strip();
    }


    private static URI requireAbsoluteUri(URI value, String fieldName) {
        if (value == null || !value.isAbsolute() || value.getHost() == null) {
            throw new IllegalArgumentException(fieldName + " must be an absolute network URI");
        }

        return value;
    }
}
