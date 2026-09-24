package application.user;

import java.util.HashMap;
import java.util.Map;

public class UserDataBase {
    private final Map<UserKey, User> usersByKey = new HashMap<>();
    private final Map<UserAddress, UserKey> keysByAddress = new HashMap<>();


    public synchronized User saveUser(User user) {
        UserAddress userAddress = createUserAddress(
            user.senderId(),
            user.chatId()
        );

        UserKey savedUserKey = keysByAddress.get(userAddress);

        if (savedUserKey != null) {
            return usersByKey.get(savedUserKey);
        }

        User savedUser = usersByKey.get(user.key());

        if (savedUser != null) return savedUser;

        usersByKey.put(user.key(), user);
        keysByAddress.put(userAddress, user.key());

        return user;
    }


    public synchronized User findUser(
        String senderId,
        String chatId
    ) {
        UserKey userKey = keysByAddress.get(
            createUserAddress(senderId, chatId)
        );

        if (userKey == null) return null;

        return usersByKey.get(userKey);
    }


    public synchronized User getUser(UserKey userKey) {
        return usersByKey.get(userKey);
    }


    public synchronized boolean containsUser(
        String senderId,
        String chatId
    ) {
        return keysByAddress.containsKey(
            createUserAddress(senderId, chatId)
        );
    }


    public synchronized boolean deleteUser(UserKey userKey) {
        User removedUser = usersByKey.remove(userKey);

        if (removedUser == null) return false;

        keysByAddress.remove(
            createUserAddress(
                removedUser.senderId(),
                removedUser.chatId()
            )
        );

        return true;
    }


    private UserAddress createUserAddress(
        String senderId,
        String chatId
    ) {
        return new UserAddress(
            senderId,
            chatId
        );
    }


    private record UserAddress(
        String senderId,
        String chatId
    ) {
    }
}
