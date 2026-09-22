package infrastructure.telegram;

import infrastructure.Delivery;
import infrastructure.Listener;
import infrastructure.telegram.TelegramApiClient.TelegramUpdate;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TelegramListener implements Listener<TelegramUpdate> {

    private static final int QUEUE_CAPACITY = 1_000;
    private static final long RETRY_DELAY_MILLIS = 1_000;

    private final TelegramApiClient apiClient;
    private final BlockingQueue<TelegramUpdate> messages = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "telegram-listener"));
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean started = new AtomicBoolean();

    private volatile CompletableFuture<List<TelegramUpdate>> currentRequest = CompletableFuture.completedFuture(List.of());
    private long offset;

    public TelegramListener(TelegramApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            running.set(true);
            executor.submit(this::listen);
        }
    }

    private void listen() {
        while (running.get()) {
            try {
                currentRequest = apiClient.getUpdates(offset);
                List<TelegramUpdate> updates = currentRequest.join();

                for (TelegramUpdate update : updates) {
                    if (isPrivateTextMessage(update)) {
                        messages.put(update);
                    }

                    offset = update.updateId() + 1;
                }
            } catch (CancellationException exception) {
                return;
            } catch (CompletionException exception) {
                retryRequest();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public Optional<Delivery<TelegramUpdate>> poll(Duration timeout) throws InterruptedException {
        TelegramUpdate update = messages.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.ofNullable(update).map(this::createDelivery);
    }

    @Override
    public boolean hasPendingMessages() {
        return !messages.isEmpty();
    }

    @Override
    public void stopReceiving() {
        if (running.compareAndSet(true, false)) {
            currentRequest.cancel(true);
            executor.shutdown();
            awaitTermination();
        }
    }

    @Override
    public void close() {
        stopReceiving();
        executor.shutdown();
        awaitTermination();
    }

    private Delivery<TelegramUpdate> createDelivery(TelegramUpdate update) {
        return new Delivery<>(update, () -> { }, () -> returnToQueue(update));
    }

    private void returnToQueue(TelegramUpdate update) {
        try {
            messages.put(update);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Cannot return Telegram update to the queue", exception);
        }
    }

    private boolean isPrivateTextMessage(TelegramUpdate update) {
        return update.message() != null
            && update.message().chat() != null
            && "private".equals(update.message().chat().type())
            && update.message().text() != null;
    }

    private void retryRequest() {
        if (!running.get()) {
            return;
        }

        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            running.set(false);
            Thread.currentThread().interrupt();
        }
    }

    private void awaitTermination() {
        boolean interrupted = false;

        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException exception) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

}
