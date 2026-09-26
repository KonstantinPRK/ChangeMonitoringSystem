package application.persistence;

import application.user.User;

/**
 * Представляет строку пользователя, прочитанную из базы данных.
 */
public class UserEntity {
    private final long id;
    private final String botId;
    private final String userId;
    private final String chatId;
    private final long subscriptionsRevision;


    public UserEntity(
            long id,
            String botId,
            String userId,
            String chatId,
            long subscriptionsRevision
    ) {
        this.id = id;
        this.botId = botId;
        this.userId = userId;
        this.chatId = chatId;
        this.subscriptionsRevision = subscriptionsRevision;
    }


    public long getId() {
        return id;
    }


    public long getSubscriptionsRevision() {
        return subscriptionsRevision;
    }


    public User toUser() {
        return new User(botId, userId, chatId);
    }
}
