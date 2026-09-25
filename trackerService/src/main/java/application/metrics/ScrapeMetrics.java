package application.metrics;

import application.connector.TrackingConnector;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScrapeMetrics {
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Counter> outcomes = new ConcurrentHashMap<>();
    private final Map<String, Counter> detectedChanges = new ConcurrentHashMap<>();


    public ScrapeMetrics(MeterRegistry meterRegistry, List<TrackingConnector> connectors) {
        this.meterRegistry = meterRegistry;
        registerMeters(connectors);
    }


    public void record(String provider, String type, String outcome, Duration duration) {
        timer(provider, type).record(duration);
        outcome(provider, type, outcome).increment();
    }


    public void changeDetected(String provider, String type) {
        String key = provider + ':' + type;
        detectedChanges.computeIfAbsent(key, ignored -> createChangeCounter(provider, type)).increment();
    }


    private Timer timer(String provider, String type) {
        String key = provider + ':' + type;
        return timers.computeIfAbsent(key, ignored -> Timer.builder("tracker.scrape.duration")
                .tag("provider", provider)
                .tag("type", type)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }


    private Counter outcome(String provider, String type, String outcome) {
        String key = provider + ':' + type + ':' + outcome;
        return outcomes.computeIfAbsent(key, ignored -> Counter.builder("tracker.scrape.total")
                .tag("provider", provider)
                .tag("type", type)
                .tag("result", outcome)
                .register(meterRegistry));
    }


    private Counter createChangeCounter(String provider, String type) {
        return Counter.builder("tracker.detected.changes")
                .tag("provider", provider)
                .tag("type", type)
                .register(meterRegistry);
    }


    private void registerMeters(List<TrackingConnector> connectors) {
        for (TrackingConnector connector : connectors) {
            String provider = connector.descriptor().key();
            for (String type : connector.descriptor().supportedKinds()) {
                registerMeters(provider, type);
            }
        }
    }


    private void registerMeters(String provider, String type) {
        String key = provider + ':' + type;
        timer(provider, type);
        outcome(provider, type, "completed");
        outcome(provider, type, "deferred");
        outcome(provider, type, "failed");
        detectedChanges.put(key, createChangeCounter(provider, type));
    }
}
