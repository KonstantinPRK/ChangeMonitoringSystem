package application.connector.stackoverflow.quota;

import application.connector.stackoverflow.StackOverflowProperties;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Кэширует данные, используемые компонентом {@code StackOverflowQuotaCache}.
 */
@Component
public class StackOverflowQuotaCache {
    private static final Duration TIME_TO_LIVE = Duration.ofDays(1);
    private final StringRedisTemplate redisTemplate;
    private final StackOverflowProperties properties;
    private final AtomicReference<StackOverflowQuota> localValue = new AtomicReference<>();


    public StackOverflowQuotaCache(
            StringRedisTemplate redisTemplate,
            StackOverflowProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }


    public Optional<StackOverflowQuota> current() {
        try {
            String value = redisTemplate.opsForValue().get(key());
            if (value != null) return Optional.of(parse(value));

        } catch (DataAccessException | IllegalArgumentException | IndexOutOfBoundsException ignored) {

        }
        return Optional.ofNullable(localValue.get());
    }


    public void update(StackOverflowQuota quota) {
        localValue.set(quota);

        try {
            redisTemplate.opsForValue().set(key(), serialize(quota), TIME_TO_LIVE);

        } catch (DataAccessException ignored) {

        }
    }


    private StackOverflowQuota parse(String value) {
        String[] parts = value.split(":", 2);
        return new StackOverflowQuota(
                Long.parseLong(parts[0]),
                java.time.Instant.ofEpochSecond(Long.parseLong(parts[1]))
        );
    }


    private String serialize(StackOverflowQuota quota) {
        return quota.remaining() + ":" + quota.blockedUntil().getEpochSecond();
    }


    private String key() {
        String apiKey = properties.key() == null ? "anonymous" : properties.key();
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(apiKey.getBytes(StandardCharsets.UTF_8));

        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);

        }
        return "stackoverflow:quota:" + HexFormat.of().formatHex(digest, 0, 8);
    }
}
