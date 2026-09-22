package application.messenger;

import java.util.Optional;

public record IncomingMessage(
    SubscriberId subscriberId,
    String text,
    Optional<String> commandCode
) {
}

