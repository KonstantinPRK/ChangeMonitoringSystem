package application.registration;

import application.messenger.MessengerClient;
import application.messenger.MessengerClientRegistry;
import application.subscription.api.SubscriptionServiceApiClient;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Координирует совместную работу компонентов через {@code BotRegistrationManager}.
 */
@Component
public class BotRegistrationManager {
    private final SubscriptionServiceApiClient apiClient;
    private final MessengerClientRegistry clientRegistry;
    private final Set<String> registeredBotIds = ConcurrentHashMap.newKeySet();
    private final Set<String> registrationsInProgress = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean running = new AtomicBoolean();


    public BotRegistrationManager(
            SubscriptionServiceApiClient apiClient,
            MessengerClientRegistry clientRegistry
    ) {
        this.apiClient = apiClient;
        this.clientRegistry = clientRegistry;
    }


    public void start() {
        if (!running.compareAndSet(false, true)) return;
        for (MessengerClient client : clientRegistry.clients()) register(client.botId());
    }


    public CompletionStage<Void> stop() {
        if (!running.getAndSet(false)) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> requests = new ArrayList<>();
        for (String botId : registeredBotIds) {
            CompletionStage<Void> request = apiClient.unregisterBot(botId)
                    .exceptionally(failure -> null);
            requests.add(request.toCompletableFuture());
        }
        registeredBotIds.clear();
        return CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new));
    }


    @Scheduled(fixedDelayString = "${app.subscription.heartbeat-interval}")
    public void confirmAvailability() {
        if (!running.get()) return;
        for (MessengerClient client : clientRegistry.clients()) confirmAvailability(client.botId());
    }


    private void confirmAvailability(String botId) {
        if (!registeredBotIds.contains(botId)) {
            register(botId);
            return;
        }

        apiClient.confirmAvailability(botId)
                .exceptionally(failure -> markUnavailable(botId));
    }


    private void register(String botId) {
        if (!registrationsInProgress.add(botId)) return;
        apiClient.registerBot(botId)
                .thenRun(() -> completeRegistration(botId))
                .exceptionally(failure -> markUnavailable(botId));
    }


    private void completeRegistration(String botId) {
        registrationsInProgress.remove(botId);
        if (running.get()) {
            registeredBotIds.add(botId);
            return;
        }

        apiClient.unregisterBot(botId);
    }


    private Void markUnavailable(String botId) {
        registrationsInProgress.remove(botId);
        registeredBotIds.remove(botId);
        return null;
    }
}
