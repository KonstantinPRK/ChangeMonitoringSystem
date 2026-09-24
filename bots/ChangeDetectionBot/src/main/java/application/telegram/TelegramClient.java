package application.telegram;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class TelegramClient {
    private static final long RETRY_DELAY_MILLISECONDS = 1_000;

    TelegramApiClient telegramApiClient;
    Executor executor;
    Consumer<TelegramMessage> messageReceiver;
    private volatile boolean running;
    private volatile CompletableFuture<List<TelegramApiClient.TelegramUpdate>>
        activeUpdateRequest;
    private volatile Thread pollingThread;
    private long offset;


    public void start() {
        if (running) return;

        running = true;
        executor.execute(this::receiveMessages);
    }


    public void stop() {
        running = false;

        CompletableFuture<List<TelegramApiClient.TelegramUpdate>> request =
            activeUpdateRequest;

        if (request != null) request.cancel(true);

        Thread thread = pollingThread;

        if (thread != null) thread.interrupt();
    }


    public void sendMessage(TelegramMessage message) {
        telegramApiClient
            .sendMessage(
                message.chatId(),
                message.text()
            )
            .join();
    }


    private void receiveMessages() {
        pollingThread = Thread.currentThread();

        try {
            while (running) {
                try {
                    List<TelegramApiClient.TelegramUpdate> updates =
                        requestUpdates();

                    processUpdates(updates);

                } catch (CancellationException exception) {
                    if (running) pauseBeforeRetry();

                } catch (CompletionException exception) {
                    if (running) pauseBeforeRetry();

                }
            }

        } finally {
            running = false;
            activeUpdateRequest = null;
            pollingThread = null;

        }
    }


    private List<TelegramApiClient.TelegramUpdate> requestUpdates() {
        activeUpdateRequest = telegramApiClient.getUpdates(offset);

        if (!running) activeUpdateRequest.cancel(true);

        return activeUpdateRequest.join();
    }


    private void processUpdates(
        List<TelegramApiClient.TelegramUpdate> updates
    ) {
        for (TelegramApiClient.TelegramUpdate update : updates) {
            offset = update.updateId() + 1;

            TelegramApiClient.TelegramIncomingMessage message =
                update.message();

            if (!isProcessable(message)) continue;

            TelegramMessage telegramMessage = new TelegramMessage(
                String.valueOf(message.from().id()),
                String.valueOf(message.chat().id()),
                message.text()
            );

            messageReceiver.accept(telegramMessage);
        }
    }


    private boolean isProcessable(
        TelegramApiClient.TelegramIncomingMessage message
    ) {
        return message != null
            && message.from() != null
            && message.chat() != null
            && message.text() != null;
    }


    private void pauseBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MILLISECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            running = false;

        }
    }
}
