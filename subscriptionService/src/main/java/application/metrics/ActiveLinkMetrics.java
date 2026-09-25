package application.metrics;

import application.persistence.LinkRepository;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ActiveLinkMetrics {
    private final LinkRepository linkRepository;

    private volatile Map<String, Long> activeLinks = Map.of();


    public ActiveLinkMetrics(LinkRepository linkRepository, MeterRegistry registry) {
        this.linkRepository = linkRepository;
        for (String type : new String[]{"github", "stackoverflow", "other"}) {
            Gauge.builder("subscription.links.active", this, metrics -> metrics.count(type))
                    .tag("type", type)
                    .register(registry);
        }
    }


    @Scheduled(fixedDelayString = "${app.metrics.links-refresh-interval:30000}")
    public void refresh() {
        Map<String, Long> domainCounts = linkRepository.countActiveByDomain();
        long github = domainCounts.getOrDefault("github.com", 0L);
        long stackOverflow = domainCounts.getOrDefault("stackoverflow.com", 0L);
        long total = 0;
        for (long count : domainCounts.values()) total += count;
        activeLinks = Map.of("github", github, "stackoverflow", stackOverflow, "other", total - github - stackOverflow);
    }


    private long count(String type) {
        return activeLinks.getOrDefault(type, 0L);
    }
}
