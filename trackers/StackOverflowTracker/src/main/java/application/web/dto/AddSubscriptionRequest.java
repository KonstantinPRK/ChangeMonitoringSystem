package application.web.dto;

import java.net.URI;
import java.util.List;

public record AddSubscriptionRequest(
    URI link,
    String botId,
    String localUserId,
    List<String> tags,
    List<String> filters
) {
}

