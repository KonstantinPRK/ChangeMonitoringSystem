package application;

import java.time.Instant;
import java.util.UUID;

public record User(String senderId, String chatId, Instant createdAt) {
}

