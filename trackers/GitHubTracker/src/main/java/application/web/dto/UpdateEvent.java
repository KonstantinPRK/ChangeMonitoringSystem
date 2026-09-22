package application.web.dto;

import java.net.URI;
import java.time.Instant;

public record UpdateEvent(
    String botId,
    String localUserId,
    URI link,
    String changeType,
    String description,
    Instant createdAt
) {
}

