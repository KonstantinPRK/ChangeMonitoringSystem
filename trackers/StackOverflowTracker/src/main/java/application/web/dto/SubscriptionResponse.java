package application.web.dto;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record SubscriptionResponse(
    UUID id,
    URI link,
    List<String> tags,
    List<String> filters
) {
}

