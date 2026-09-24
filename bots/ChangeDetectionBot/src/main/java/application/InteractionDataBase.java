package application;

import java.util.concurrent.ConcurrentHashMap;

public class InteractionDataBase {
    ConcurrentHashMap<User, InteractionChannel> interactions;

    public boolean interactionChannelIsOpen(User user) {
        return interactions.get(user).isOpen();
    }

    public InteractionChannel getInteractionChannel(User user) {
        return interactions.get(user);
    }

    public void saveNewInteractionChannel(User user, InteractionChannel channel) {
        interactions.putIfAbsent(user, channel);
    }


    public InteractionChannel getInteraction(User user) {
        return interactions.get(user);
    }
}
