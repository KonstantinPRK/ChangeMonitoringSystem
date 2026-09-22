package application.infrastructure.messaging;

public interface MessageProcessor<I, O> extends AutoCloseable {
    O process(I input);

    @Override
    default void close() {
    }
}

