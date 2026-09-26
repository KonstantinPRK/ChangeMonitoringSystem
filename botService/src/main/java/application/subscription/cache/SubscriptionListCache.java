package application.subscription.cache;

import application.config.SubscriptionProperties;
import application.subscription.api.SubscriptionView;
import application.user.UserKey;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Кэширует данные, используемые компонентом {@code SubscriptionListCache}.
 */
@Component
public class SubscriptionListCache {
    private final StringRedisTemplate redisTemplate;
    private final SubscriptionProperties subscriptionProperties;
    private final ObjectMapper objectMapper;


    public SubscriptionListCache(
            StringRedisTemplate redisTemplate,
            SubscriptionProperties subscriptionProperties,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.subscriptionProperties = subscriptionProperties;
        this.objectMapper = objectMapper;
    }


    public Optional<List<SubscriptionView>> find(UserKey user) {
        try {
            String json = redisTemplate.opsForValue().get(key(user));
            if (json == null) return Optional.empty();

            SubscriptionView[] subscriptions = objectMapper.readValue(json, SubscriptionView[].class);
            return Optional.of(Arrays.asList(subscriptions));

        } catch (JacksonException exception) {
            invalidate(user);
            return Optional.empty();

        } catch (DataAccessException exception) {
            return Optional.empty();

        }
    }


    public void save(UserKey user, List<SubscriptionView> subscriptions) {
        try {
            String json = objectMapper.writeValueAsString(subscriptions);
            Duration timeToLive = Duration.ofSeconds(subscriptionProperties.cacheTtlSeconds());
            redisTemplate.opsForValue().set(key(user), json, timeToLive);

        } catch (DataAccessException ignored) {

        }
    }


    public void invalidate(UserKey user) {
        try {
            redisTemplate.delete(key(user));

        } catch (DataAccessException ignored) {

        }
    }


    private String key(UserKey user) {
        return "subscriptions:" + user.botId() + ':' + user.externalUserId() + ':' + user.chatId();
    }
}
