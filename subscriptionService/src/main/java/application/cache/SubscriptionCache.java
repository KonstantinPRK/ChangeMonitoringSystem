package application.cache;

import application.user.SubscriptionView;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionCache {
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final MeterRegistry metrics;
    private final Duration ttl;


    public SubscriptionCache(
            StringRedisTemplate redis,
            ObjectMapper json,
            MeterRegistry metrics,
            @Value("${app.cache.ttl:300s}") Duration ttl
    ) {
        this.redis = redis;
        this.json = json;
        this.metrics = metrics;
        this.ttl = ttl;
        if (ttl.isZero() || ttl.isNegative()) throw new IllegalArgumentException("Cache TTL must be positive");
    }


    public Optional<List<SubscriptionView>> get(String key) {
        try {
            String value = redis.opsForValue().get(key);
            if (value == null) {
                metrics.counter("subscription.cache", "result", "miss").increment();
                return Optional.empty();
            }
            List<SubscriptionView> result = json.readValue(value, new TypeReference<List<SubscriptionView>>() {});
            metrics.counter("subscription.cache", "result", "hit").increment();
            return Optional.of(result);

        } catch (RuntimeException exception) {
            metrics.counter("subscription.cache", "result", "error").increment();
            return Optional.empty();

        }
    }


    public void put(String key, List<SubscriptionView> subscriptions) {
        try {
            redis.opsForValue().set(key, json.writeValueAsString(subscriptions), ttl);

        } catch (RuntimeException exception) {
            metrics.counter("subscription.cache", "result", "error").increment();

        }
    }
}
