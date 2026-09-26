package application.notification.outbox;

import application.notification.model.StoredNotification;
import application.notification.transport.LinkNotificationPublisher;
import application.notification.transport.PublicationResult;
import application.work.NotificationWorkSignal;
import application.work.SerializedWorker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Передаёт ожидающие задачи подходящим обработчикам через {@code NotificationDispatcher}.
 */
@Component
public class NotificationDispatcher {
    private final NotificationOutboxRepository outboxRepository;
    private final LinkNotificationPublisher notificationPublisher;
    private final NotificationCompletion notificationCompletion;
    private final SerializedWorker worker;
    private final AtomicBoolean running = new AtomicBoolean();


    public NotificationDispatcher(
            NotificationOutboxRepository outboxRepository,
            LinkNotificationPublisher notificationPublisher,
            NotificationCompletion notificationCompletion,
            NotificationWorkSignal workSignal,
            @Qualifier("trackerTaskExecutor") Executor executor
    ) {
        this.outboxRepository = outboxRepository;
        this.notificationPublisher = notificationPublisher;
        this.notificationCompletion = notificationCompletion;
        worker = new SerializedWorker(executor, this::publishNext);
        workSignal.connect(worker::signal);
    }


    public void start() {
        running.set(true);
        worker.signal();
    }


    public void stop() {
        running.set(false);
    }


    @Scheduled(fixedDelayString = "${app.workers.recovery-interval}")
    public void recover() {
        if (running.get()) worker.signal();
    }


    private CompletionStage<Boolean> publishNext() {
        if (!running.get()) return CompletableFuture.completedFuture(false);
        Optional<StoredNotification> claimedNotification = outboxRepository.claimNext();
        if (claimedNotification.isEmpty()) return CompletableFuture.completedFuture(false);

        StoredNotification notification = claimedNotification.get();
        return notificationPublisher.publish(notification)
                .handle((result, failure) -> complete(notification, result, failure));
    }


    private boolean complete(
            StoredNotification notification,
            PublicationResult result,
            Throwable failure
    ) {
        if (failure == null) notificationCompletion.complete(notification, result);
        else notificationCompletion.fail(notification, unwrap(failure));
        return true;
    }


    private Throwable unwrap(Throwable failure) {
        if (failure instanceof CompletionException && failure.getCause() != null) return failure.getCause();
        return failure;
    }
}
