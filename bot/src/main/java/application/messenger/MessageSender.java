package application.messenger;

@FunctionalInterface
public interface MessageSender {
    void send(SubscriberId subscriberId, String text);
}
