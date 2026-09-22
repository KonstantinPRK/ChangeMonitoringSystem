package infrastructure;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface Sender<T> {
    CompletableFuture<Void> send(T message);

    default void close() {
    }
}
