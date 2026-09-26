package application.connector.github.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Реализует ответственность компонента {@code GitHubRateLimitGate}.
 */
@Component
public class GitHubRateLimitGate {
    private final GitHubRateLimitCache rateLimitCache;
    private final Clock clock;


    public GitHubRateLimitGate(GitHubRateLimitCache rateLimitCache, Clock clock) {
        this.rateLimitCache = rateLimitCache;
        this.clock = clock;
    }


    public Optional<Instant> blockedUntil() {
        return rateLimitCache.current()
                .filter(limit -> limit.remaining() <= 0)
                .map(GitHubRateLimit::resetAt)
                .filter(resetAt -> resetAt.isAfter(clock.instant()));
    }
}
