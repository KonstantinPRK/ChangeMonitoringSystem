package infrastructure.tracker.http;

import java.net.URI;

public record AddSubscriptionRequest(URI resource, String botId, String localUserId) {
}
