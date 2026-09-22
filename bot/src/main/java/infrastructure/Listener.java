package infrastructure;

import java.time.Duration;
import java.util.Optional;

public interface Listener<T> {
    void start();

    Optional<Delivery<T>> poll(Duration timeout) throws InterruptedException;

    boolean hasPendingMessages();

    void stopReceiving();

    void close();
}
