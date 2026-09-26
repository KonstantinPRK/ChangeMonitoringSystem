package application.catalog.synchronization;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Планирует периодический запуск задач через {@code LinkSynchronizationScheduler}.
 */
@Component
public class LinkSynchronizationScheduler {
    private final LinkSynchronizationService synchronizationService;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean synchronizationInProgress = new AtomicBoolean();


    public LinkSynchronizationScheduler(LinkSynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }


    public void start() {
        running.set(true);
        synchronize();
    }


    public void stop() {
        running.set(false);
    }


    public void requestSynchronization() {
        synchronize();
    }


    @Scheduled(fixedDelayString = "${app.subscription-service.synchronization-interval}")
    public void synchronize() {
        if (!running.get() || !synchronizationInProgress.compareAndSet(false, true)) return;
        synchronizationService.synchronize()
                .whenComplete((ignored, failure) -> synchronizationInProgress.set(false));
    }
}
