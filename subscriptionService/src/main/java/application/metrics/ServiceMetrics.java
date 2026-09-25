package application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

@Component
public class ServiceMetrics {
    private static final String[] DELIVERY_KINDS = {
            "subscription",
            "link_notification",
            "tracker_request",
            "bot_notification",
            "dlq",
            "other"
    };
    private static final String[] ACTIONS = {"save", "delete", "other"};
    private final MeterRegistry registry;
    private final Counter acceptedSubscriptions;
    private final Counter acceptedUpdates;


    public ServiceMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.acceptedSubscriptions = registry.counter("subscription.requests.accepted");
        this.acceptedUpdates = registry.counter("subscription.updates.accepted");
        registerDeliveryMetrics();
        registerSubscriptionMetrics();
    }


    public void acceptedSubscription() {
        afterCommit(acceptedSubscriptions::increment);
    }


    public void acceptedUpdate() {
        afterCommit(acceptedUpdates::increment);
    }


    public void subscriptionProcessed(String action, boolean changed) {
        String actionLabel = switch (action) {
            case "SAVE", "save" -> "save";
            case "DELETE", "delete" -> "delete";
            default -> "other";
        };
        afterCommit(() -> registry.counter(
                "subscription.requests.processed",
                "action",
                actionLabel,
                "changed",
                Boolean.toString(changed)
        ).increment());
    }


    public void delivered(String kind) {
        registry.counter("subscription.deliveries", "kind", deliveryKind(kind)).increment();
    }


    public void failed(String kind) {
        registry.counter("subscription.failures", "kind", deliveryKind(kind)).increment();
    }


    public void recordDelivery(String kind, long durationNanos) {
        registry.timer("subscription.delivery.duration", "kind", deliveryKind(kind)).record(durationNanos, TimeUnit.NANOSECONDS);
    }


    private void registerDeliveryMetrics() {
        for (String kind : DELIVERY_KINDS) {
            registry.counter("subscription.deliveries", "kind", kind);
            registry.counter("subscription.failures", "kind", kind);
            registry.timer("subscription.delivery.duration", "kind", kind);
        }
    }


    private void registerSubscriptionMetrics() {
        for (String action : ACTIONS) {
            registry.counter("subscription.requests.processed", "action", action, "changed", "true");
            registry.counter("subscription.requests.processed", "action", action, "changed", "false");
        }
    }


    private String deliveryKind(String kind) {
        return switch (kind) {
            case "subscription", "link_notification", "tracker_request", "bot_notification", "dlq" -> kind;
            default -> "other";
        };
    }


    private void afterCommit(Runnable record) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            record.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                record.run();
            }
        });
    }
}
