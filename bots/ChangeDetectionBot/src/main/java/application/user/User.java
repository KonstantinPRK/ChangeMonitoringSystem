package application.user;

import java.time.Instant;

public record User(
    UserKey key,
    String senderId,
    String chatId,
    Instant createdAt
) {
}
