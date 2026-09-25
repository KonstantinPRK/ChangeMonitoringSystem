package application.user;

import java.util.List;

public record SubscriberBatch(List<User> users, long lastUserId) {
}
