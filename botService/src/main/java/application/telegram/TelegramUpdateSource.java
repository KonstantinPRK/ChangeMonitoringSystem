package application.telegram;

import application.config.TelegramProperties;
import application.inbox.IncomingMessageRepository;
import application.messenger.IncomingMessageBatch;
import application.messenger.IncomingMessageConsumer;
import application.messenger.MessageUpdateSource;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TelegramUpdateSource implements MessageUpdateSource {
    private final TelegramApiClient apiClient;
    private final TelegramUpdateParser updateParser;
    private final IncomingMessageRepository messageRepository;
    private final TelegramProperties telegramProperties;
    private final TaskScheduler taskScheduler;
    private final Clock clock;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean requestInProgress = new AtomicBoolean();

    private IncomingMessageConsumer messageConsumer;


    public TelegramUpdateSource(
            TelegramApiClient apiClient,
            TelegramUpdateParser updateParser,
            IncomingMessageRepository messageRepository,
            TelegramProperties telegramProperties,
            TaskScheduler taskScheduler,
            Clock clock
    ) {
        this.apiClient = apiClient;
        this.updateParser = updateParser;
        this.messageRepository = messageRepository;
        this.telegramProperties = telegramProperties;
        this.taskScheduler = taskScheduler;
        this.clock = clock;
    }


    @Override
    public void start(IncomingMessageConsumer consumer) {
        messageConsumer = consumer;
        if (running.compareAndSet(false, true)) scheduleNext(Duration.ZERO);
    }


    @Override
    public void stop() {
        running.set(false);
    }


    private void requestUpdates() {
        if (!running.get() || !requestInProgress.compareAndSet(false, true)) return;

        long offset = messageRepository.checkpoint(telegramProperties.botId());
        Map<String, Object> request = Map.of(
                "offset", offset,
                "timeout", telegramProperties.longPollTimeout().toSeconds(),
                "allowed_updates", List.of("message")
        );
        apiClient.post("getUpdates", request)
                .whenComplete((updates, failure) -> completeRequest(offset, updates, failure));
    }


    private void completeRequest(long offset, JsonNode updates, Throwable failure) {
        requestInProgress.set(false);
        if (!running.get()) return;

        if (failure == null) {
            acceptUpdates(offset, updates);
            return;
        }

        scheduleNext(telegramProperties.retryDelay());
    }


    private void acceptUpdates(long offset, JsonNode updates) {
        try {
            IncomingMessageBatch batch = updateParser.parse(updates, offset);
            messageConsumer.accept(batch);
            scheduleNext(Duration.ZERO);

        } catch (RuntimeException exception) {
            scheduleNext(telegramProperties.retryDelay());

        }
    }


    private void scheduleNext(java.time.Duration delay) {
        Instant startTime = clock.instant().plus(delay);
        taskScheduler.schedule(this::requestUpdates, startTime);
    }
}
