package application;

import java.time.Instant;

public record User(String senderId, String chatId, Instant createdAt) {
}

