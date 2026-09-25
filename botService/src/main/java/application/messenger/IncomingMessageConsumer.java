package application.messenger;

@FunctionalInterface
public interface IncomingMessageConsumer {
    void accept(IncomingMessageBatch batch);
}
