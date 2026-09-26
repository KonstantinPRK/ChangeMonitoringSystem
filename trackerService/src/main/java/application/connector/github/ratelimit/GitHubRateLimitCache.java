package application.connector.github.ratelimit;

import application.connector.github.GitHubProperties;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Кэширует данные, используемые компонентом {@code GitHubRateLimitCache}.
 */
@Component
public class GitHubRateLimitCache {
    private final StringRedisTemplate redisTemplate;
    private final GitHubProperties gitHubProperties;
    private final Clock clock;
    private final AtomicReference<GitHubRateLimit> localValue = new AtomicReference<>();


    public GitHubRateLimitCache(
            StringRedisTemplate redisTemplate,
            GitHubProperties gitHubProperties,
            Clock clock
    ) {
        this.redisTemplate = redisTemplate;
        this.gitHubProperties = gitHubProperties;
        this.clock = clock;
    }


    public Optional<GitHubRateLimit> current() {
        try {
            String value = redisTemplate.opsForValue().get(key());
            if (value != null) return Optional.of(parse(value));

        } catch (DataAccessException | IllegalArgumentException | IndexOutOfBoundsException ignored) {

        }
        return Optional.ofNullable(localValue.get());
    }


    public void update(GitHubRateLimit rateLimit) {
        localValue.set(rateLimit);

        try {
            Duration timeToLive = Duration.between(clock.instant(), rateLimit.resetAt()).plusMinutes(1);
            if (timeToLive.isNegative()) timeToLive = Duration.ofMinutes(1);
            redisTemplate.opsForValue().set(key(), serialize(rateLimit), timeToLive);

        } catch (DataAccessException ignored) {

        }
    }


    private GitHubRateLimit parse(String value) {
        String[] parts = value.split(":", 2);
        return new GitHubRateLimit(Long.parseLong(parts[0]), java.time.Instant.ofEpochSecond(
                Long.parseLong(parts[1])
        ));
    }


    private String serialize(GitHubRateLimit rateLimit) {
        return rateLimit.remaining() + ":" + rateLimit.resetAt().getEpochSecond();
    }


    private String key() {
        String token = gitHubProperties.token() == null ? "anonymous" : gitHubProperties.token();
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));

        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);

        }
        return "github:rate-limit:" + HexFormat.of().formatHex(digest, 0, 8);
    }
}
