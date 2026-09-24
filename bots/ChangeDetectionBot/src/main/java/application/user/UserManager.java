package application.user;

import application.telegram.TelegramMessage;

import java.time.Instant;
import java.util.UUID;

public class UserManager {
    UserDataBase userDataBase;
    String botId;


    public User resolveUser(TelegramMessage message) {
        User savedUser = userDataBase.findUser(
            message.senderId(),
            message.chatId()
        );

        if (savedUser != null) return savedUser;

        User user = new User(
            new UserKey(
                botId,
                UUID.randomUUID().toString()
            ),
            message.senderId(),
            message.chatId(),
            Instant.now()
        );

        return userDataBase.saveUser(user);
    }


    public User getUser(UserKey userKey) {
        return userDataBase.getUser(userKey);
    }


    public boolean containsUser(
        String senderId,
        String chatId
    ) {
        return userDataBase.containsUser(
            senderId,
            chatId
        );
    }


    public boolean deleteUser(UserKey userKey) {
        return userDataBase.deleteUser(userKey);
    }
}
