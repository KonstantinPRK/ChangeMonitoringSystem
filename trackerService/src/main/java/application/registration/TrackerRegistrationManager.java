package application.registration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Координирует совместную работу компонентов через {@code TrackerRegistrationManager}.
 */
@Component
public class TrackerRegistrationManager {
    private final TrackerRegistrationApiClient apiClient;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean registered = new AtomicBoolean();


    public TrackerRegistrationManager(TrackerRegistrationApiClient apiClient) {
        this.apiClient = apiClient;
    }


    public void start() {
        running.set(true);
        register();
    }


    public CompletionStage<Void> stop() {
        running.set(false);
        if (!registered.getAndSet(false)) return CompletableFuture.completedFuture(null);
        return apiClient.unregister().exceptionally(failure -> null);
    }


    @Scheduled(fixedDelayString = "${app.tracker.availability-interval}")
    public void confirmAvailability() {
        if (!running.get()) return;
        if (!registered.get()) {
            register();
            return;
        }

        apiClient.confirmAvailability().exceptionally(this::markUnavailable);
    }


    private void register() {
        apiClient.register()
                .thenRun(this::completeRegistration)
                .exceptionally(this::markUnavailable);
    }


    private void completeRegistration() {
        if (running.get()) {
            registered.set(true);
            return;
        }

        apiClient.unregister();
    }


    private Void markUnavailable(Throwable failure) {
        registered.set(false);
        return null;
    }
}
