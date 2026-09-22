package application.infrastructure.messaging;

import java.time.Duration;
import java.util.Optional;

public interface MessageListener<T> extends AutoCloseable {
    void start();

    Optional<Delivery<T>> poll(Duration timeout) throws InterruptedException;

    boolean hasPendingMessages();

    void stopReceiving();

    @Override
    void close();
}

