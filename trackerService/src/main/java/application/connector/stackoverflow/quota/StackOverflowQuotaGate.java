package application.connector.stackoverflow.quota;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Реализует ответственность компонента {@code StackOverflowQuotaGate}.
 */
@Component
public class StackOverflowQuotaGate {
    private final StackOverflowQuotaCache quotaCache;
    private final Clock clock;


    public StackOverflowQuotaGate(StackOverflowQuotaCache quotaCache, Clock clock) {
        this.quotaCache = quotaCache;
        this.clock = clock;
    }


    public Optional<Instant> blockedUntil() {
        return quotaCache.current()
                .map(StackOverflowQuota::blockedUntil)
                .filter(blockedUntil -> blockedUntil.isAfter(clock.instant()));
    }
}
