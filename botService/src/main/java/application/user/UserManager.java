package application.user;

import application.messenger.MessengerClient;
import application.messenger.MessengerClientRegistry;

import org.springframework.stereotype.Service;

@Service
public class UserManager {
    private final MessengerClientRegistry clientRegistry;
    private final UserRepository userRepository;


    public UserManager(
            MessengerClientRegistry clientRegistry,
            UserRepository userRepository
    ) {
        this.clientRegistry = clientRegistry;
        this.userRepository = userRepository;
    }


    public BotUser resolve(String botId, String externalUserId, String chatId) {
        MessengerClient client = clientRegistry.get(botId);
        UserKey key = new UserKey(botId, externalUserId, chatId);
        return userRepository.findOrCreate(key, client.messengerType());
    }


    public BotUser find(UserKey key) {
        return userRepository.find(key).orElseThrow();
    }


    public void activate(BotUser user) {
        userRepository.activate(user.id());
    }


    public void delete(BotUser user) {
        userRepository.markDeleted(user.id());
    }
}
