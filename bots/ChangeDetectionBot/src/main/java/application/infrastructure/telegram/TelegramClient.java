package application.infrastructure.telegram;

import application.infrastructure.messaging.CommunicationClient;
import application.infrastructure.messaging.Delivery;
import application.infrastructure.messaging.MessageListener;
import application.infrastructure.messaging.MessageProcessor;
import application.infrastructure.messaging.MessageSender;
import application.infrastructure.telegram.TelegramApiClient.TelegramUpdate;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TelegramClient implements CommunicationClient {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(500);

    private final MessageListener<TelegramUpdate> listener;
    private final MessageProcessor<TelegramUpdate, TelegramMessage> sourcer;
    private final MessageSender<TelegramMessage> sender;
    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "telegram-dispatcher"));
    private final ExecutorService workers;
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean receivingStopped = new AtomicBoolean(true);
    private final AtomicInteger inFlight = new AtomicInteger();

    public TelegramClient(
        MessageListener<TelegramUpdate> listener,
        MessageProcessor<TelegramUpdate, TelegramMessage> sourcer,
        MessageSender<TelegramMessage> sender,
        int workerCount
    ) {
        if (workerCount < 1) {
            throw new IllegalArgumentException("workerCount must be greater than zero");
        }

        this.listener = listener;
        this.sourcer = sourcer;
        this.sender = sender;
        this.workers = Executors.newFixedThreadPool(workerCount, runnable -> new Thread(runnable, "telegram-worker"));
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            running.set(true);
            receivingStopped.set(false);
            dispatcher.submit(this::working);
            listener.start();
        }
    }

    private void working() {
        while (!receivingStopped.get() || listener.hasPendingMessages() || inFlight.get() > 0) {
            try {
                Optional<Delivery<TelegramUpdate>> delivery = listener.poll(POLL_TIMEOUT);
                delivery.ifPresent(this::process);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void process(Delivery<TelegramUpdate> delivery) {
        inFlight.incrementAndGet();

        workers.submit(() -> {
            try {
                TelegramMessage response = sourcer.process(delivery.message());
                sender.send(response).join();
                delivery.acknowledge();
            } catch (RuntimeException exception) {
                delivery.retry();
            } finally {
                inFlight.decrementAndGet();
            }
        });
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        listener.stopReceiving();
        receivingStopped.set(true);

        dispatcher.shutdown();
        awaitTermination(dispatcher);

        workers.shutdown();
        awaitTermination(workers);

        closeResources();
    }

    private void closeResources() {
        try {
            sender.close();
        } finally {
            try {
                sourcer.close();
            } finally {
                listener.close();
            }
        }
    }

    private void awaitTermination(ExecutorService executor) {
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
