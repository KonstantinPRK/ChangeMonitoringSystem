package application.subscription;

import application.user.UserKey;

public record Subscription(
    UserKey userKey,
    Link link
) {
}
