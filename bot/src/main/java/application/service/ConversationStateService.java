package application.service;

import application.messenger.SubscriberId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConversationStateService {
    private final Map<SubscriberId, ConversationState> states = new ConcurrentHashMap<>();

    public ConversationState getState(SubscriberId subscriberId) {
        return states.getOrDefault(subscriberId, ConversationState.IDLE);
    }

    public void setState(SubscriberId subscriberId, ConversationState state) {
        if (state == ConversationState.IDLE) {
            clear(subscriberId);
            return;
        }

        states.put(subscriberId, state);
    }

    public void clear(SubscriberId subscriberId) {
        states.remove(subscriberId);
    }
}
