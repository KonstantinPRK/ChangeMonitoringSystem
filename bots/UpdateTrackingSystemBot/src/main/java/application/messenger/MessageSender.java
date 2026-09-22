package application.messenger;

public interface MessageSender {
    void send(SubscriberId subscriberId, String text);
}

