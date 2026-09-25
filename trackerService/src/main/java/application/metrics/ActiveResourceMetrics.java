package application.metrics;

import application.catalog.persistence.LinkCatalogRepository;
import application.connector.TrackingConnector;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ActiveResourceMetrics {
    private final LinkCatalogRepository linkCatalogRepository;
    private final Map<String, AtomicLong> activeResources = new HashMap<>();


    public ActiveResourceMetrics(
            LinkCatalogRepository linkCatalogRepository,
            List<TrackingConnector> connectors,
            MeterRegistry meterRegistry
    ) {
        this.linkCatalogRepository = linkCatalogRepository;
        registerGauges(connectors, meterRegistry);
    }


    @Scheduled(fixedDelayString = "${app.subscription-service.synchronization-interval}")
    public void refresh() {
        activeResources.forEach((key, value) -> {
            String[] parts = key.split(":", 2);
            value.set(linkCatalogRepository.countActive(parts[0], parts[1]));
        });
    }


    private void registerGauges(
            List<TrackingConnector> connectors,
            MeterRegistry meterRegistry
    ) {
        for (TrackingConnector connector : connectors) {
            String provider = connector.descriptor().key();
            for (String type : connector.descriptor().supportedKinds()) {
                registerGauge(provider, type, meterRegistry);
            }
        }
    }


    private void registerGauge(String provider, String type, MeterRegistry meterRegistry) {
        AtomicLong value = new AtomicLong();
        activeResources.put(provider + ':' + type, value);
        Gauge.builder("tracker.active.resources", value, AtomicLong::get)
                .tag("provider", provider)
                .tag("type", type)
                .register(meterRegistry);
    }
}
