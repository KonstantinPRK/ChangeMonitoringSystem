package application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Регистрирует и обновляет метрики компонента {@code NotificationMetrics}.
 */
@Component
public class NotificationMetrics {
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> publications = new ConcurrentHashMap<>();


    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        publications.put("completed", createCounter("completed"));
        publications.put("retry", createCounter("retry"));
        publications.put("failed", createCounter("failed"));
    }


    public void record(String result) {
        publications.computeIfAbsent(result, this::createCounter).increment();
    }


    private Counter createCounter(String result) {
        return Counter.builder("tracker.notifications.published")
                .tag("result", result)
                .register(meterRegistry);
    }
}
