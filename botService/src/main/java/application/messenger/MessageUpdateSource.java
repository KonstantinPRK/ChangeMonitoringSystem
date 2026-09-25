package application.messenger;

public interface MessageUpdateSource {
    void start(IncomingMessageConsumer consumer);

    void stop();
}
