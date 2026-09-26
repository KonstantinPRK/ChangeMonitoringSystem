package application.queue;

import application.metrics.ServiceMetrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Обрабатывает полученную задачу через {@code QueueProcessor}.
 */
@Component
public class QueueProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueProcessor.class);
    private final QueueStore queue;
    private final TransactionTemplate transaction;
    private final ServiceMetrics metrics;
    private final int batchSize;
    private final int leaseSeconds;
    private final ReentrantReadWriteLock lifecycle = new ReentrantReadWriteLock(true);

    private volatile boolean running;


    public QueueProcessor(
            QueueStore queue,
            org.springframework.transaction.PlatformTransactionManager transactionManager,
            ServiceMetrics metrics,
            @Value("${app.queue.batch-size:32}") int batchSize,
            @Value("${app.queue.lease-seconds:60}") int leaseSeconds
    ) {
        this.queue = queue;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        this.transaction.setTimeout(20);
        this.metrics = metrics;
        this.batchSize = batchSize;
        this.leaseSeconds = leaseSeconds;
        if (batchSize < 1 || leaseSeconds < 30) throw new IllegalArgumentException("Invalid queue batch size or lease");
    }


    public void start() {
        lifecycle.writeLock().lock();
        try {
            running = true;

        } finally {
            lifecycle.writeLock().unlock();

        }
    }


    public void stop() {
        running = false;
        try {
            if (lifecycle.writeLock().tryLock(25, TimeUnit.SECONDS)) {
                lifecycle.writeLock().unlock();
            } else {
                LOGGER.warn("Drain timeout reached; unfinished messages remain in PostgreSQL");
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


    public void process(QueueKind kind, boolean atomic, Consumer<String> handler) {
        for (int index = 0; index < batchSize; index++) {
            lifecycle.readLock().lock();
            try {
                if (!running) return;
                Optional<QueueMessage> message = queue.claim(kind, leaseSeconds);
                if (message.isEmpty()) return;
                processMessage(message.get(), atomic, handler);

            } finally {
                lifecycle.readLock().unlock();

            }
        }
    }


    private void processMessage(QueueMessage message, boolean atomic, Consumer<String> handler) {
        long started = System.nanoTime();
        try {
            if (atomic) {
                Boolean processed = transaction.execute(status -> processAtomically(message, handler));
                if (!Boolean.TRUE.equals(processed)) return;
            } else {
                handler.accept(message.payload());
                queue.complete(message);
            }
            metrics.delivered(message.kind().name().toLowerCase());

        } catch (RuntimeException exception) {
            queue.fail(message, exception instanceof IllegalArgumentException, exception.getClass().getSimpleName());
            metrics.failed(message.kind().name().toLowerCase());
            LOGGER.warn("Processing {} message {} failed: {}", message.kind(), message.messageId(), exception.getClass().getSimpleName());

        } finally {
            metrics.recordDelivery(message.kind().name().toLowerCase(), System.nanoTime() - started);

        }
    }


    private boolean processAtomically(QueueMessage message, Consumer<String> handler) {
        if (!queue.lockClaim(message)) return false;
        handler.accept(message.payload());
        queue.complete(message);
        return true;
    }
}
