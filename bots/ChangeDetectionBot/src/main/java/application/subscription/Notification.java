package application.subscription;

import application.user.UserKey;

public record Notification(
    UserKey userKey,
    String message
) {
}
