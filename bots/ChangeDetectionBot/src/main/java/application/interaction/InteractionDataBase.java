package application.interaction;

import application.user.UserKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InteractionDataBase {
    private final ConcurrentMap<UserKey, InteractionChannel> interactions =
        new ConcurrentHashMap<>();


    public boolean interactionChannelIsOpen(UserKey userKey) {
        InteractionChannel interactionChannel = interactions.get(userKey);

        return interactionChannel != null
            && interactionChannel.isOpen();
    }


    public InteractionChannel getInteractionChannel(UserKey userKey) {
        return interactions.get(userKey);
    }


    public void saveNewInteractionChannel(
        UserKey userKey,
        InteractionChannel interactionChannel
    ) {
        interactions.putIfAbsent(
            userKey,
            interactionChannel
        );
    }


    public InteractionChannel getInteraction(UserKey userKey) {
        return interactions.get(userKey);
    }
}
