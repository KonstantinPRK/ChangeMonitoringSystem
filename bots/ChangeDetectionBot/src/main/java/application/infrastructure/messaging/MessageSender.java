package application.infrastructure.messaging;

import java.util.concurrent.CompletableFuture;

public interface MessageSender<T> extends AutoCloseable {
    CompletableFuture<Void> send(T message);

    @Override
    default void close() {
    }
}

