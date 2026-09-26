package application.vk;

import application.config.VkProperties;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Получает входные данные из внешнего источника через {@code VkUpdateSource}.
 */
@Component
public class VkUpdateSource implements MessageUpdateSource {
    private final VkApiClient apiClient;
    private final VkUpdateParser updateParser;
    private final IncomingMessageRepository messageRepository;
    private final VkProperties vkProperties;
    private final TaskScheduler taskScheduler;
    private final Clock clock;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean requestInProgress = new AtomicBoolean();

    private volatile VkLongPollSession session;
    private volatile boolean useServerTimestamp;
    private IncomingMessageConsumer messageConsumer;


    public VkUpdateSource(
            VkApiClient apiClient,
            VkUpdateParser updateParser,
            IncomingMessageRepository messageRepository,
            VkProperties vkProperties,
            TaskScheduler taskScheduler,
            Clock clock
    ) {
        this.apiClient = apiClient;
        this.updateParser = updateParser;
        this.messageRepository = messageRepository;
        this.vkProperties = vkProperties;
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
        if (session == null) {
            requestLongPollSession();
            return;
        }

        long timestamp = messageRepository.checkpoint(vkProperties.botId());
        if (timestamp == 0L) timestamp = session.timestamp();
        apiClient.getUpdates(session, timestamp)
                .whenComplete(this::completeUpdateRequest);
    }


    private void requestLongPollSession() {
        apiClient.getLongPollServer()
                .whenComplete(this::completeSessionRequest);
    }


    private void completeSessionRequest(VkLongPollSession openedSession, Throwable failure) {
        requestInProgress.set(false);
        if (!running.get()) return;
        if (failure != null) {
            scheduleNext(vkProperties.retryDelay());
            return;
        }

        session = openedSession;
        long storedTimestamp = messageRepository.checkpoint(vkProperties.botId());
        long nextTimestamp = useServerTimestamp || storedTimestamp == 0L
                ? openedSession.timestamp()
                : storedTimestamp;
        useServerTimestamp = false;
        acceptTimestamp(nextTimestamp);
    }


    private void completeUpdateRequest(JsonNode response, Throwable failure) {
        requestInProgress.set(false);
        if (!running.get()) return;
        if (failure != null) {
            scheduleNext(vkProperties.retryDelay());
            return;
        }

        try {
            if (response.has("failed")) {
                handleLongPollFailure(response);
                return;
            }

            IncomingMessageBatch batch = updateParser.parse(response);
            messageConsumer.accept(batch);
            scheduleNext(Duration.ZERO);

        } catch (RuntimeException exception) {
            scheduleNext(vkProperties.retryDelay());

        }
    }


    private void handleLongPollFailure(JsonNode response) {
        int failureCode = response.path("failed").asInt();
        if (failureCode == 1 && response.hasNonNull("ts")) {
            acceptTimestamp(response.path("ts").asLong());
            return;
        }

        session = null;
        useServerTimestamp = true;
        scheduleNext(vkProperties.retryDelay());
    }


    private void acceptTimestamp(long timestamp) {
        try {
            IncomingMessageBatch checkpoint = new IncomingMessageBatch(
                    vkProperties.botId(),
                    timestamp,
                    List.of()
            );
            messageConsumer.accept(checkpoint);
            scheduleNext(Duration.ZERO);

        } catch (RuntimeException exception) {
            scheduleNext(vkProperties.retryDelay());

        }
    }


    private void scheduleNext(Duration delay) {
        Instant startTime = clock.instant().plus(delay);
        taskScheduler.schedule(this::requestUpdates, startTime);
    }
}
