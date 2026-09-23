package application.bot;

import application.registration.RemoteSystemStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BotRegistry {
    private final ConcurrentMap<String, BotInstance> botsById = new ConcurrentHashMap<>();

    public void register(BotInstance bot) {
        botsById.put(bot.botId(), bot);
    }


    public BotInstance confirmAvailability(String botId, Instant confirmedUntil) {
        BotInstance updated = botsById.computeIfPresent(
            requireId(botId),
            (id, bot) -> bot.confirmAvailability(confirmedUntil)
        );

        if (updated == null) {
            throw new IllegalArgumentException("Unknown bot: " + botId);
        }

        return updated;
    }


    public void unregister(String botId) {
        botsById.remove(requireId(botId));
    }


    public Optional<BotInstance> find(String botId) {
        return Optional.ofNullable(botsById.get(requireId(botId)));
    }


    public Optional<BotInstance> findActive(String botId, Instant now) {
        return find(botId).filter(bot -> isActive(bot, now));
    }


    public List<BotInstance> findAll() {
        return List.copyOf(botsById.values());
    }


    public void markExpiredUnavailable(Instant now) {
        botsById.replaceAll((id, bot) -> {
            if (bot.status() == RemoteSystemStatus.ACTIVE
                && !bot.availabilityConfirmedUntil().isAfter(now)) {
                return bot.markUnavailable();
            }

            return bot;
        });
    }


    public int size() {
        return botsById.size();
    }


    private static boolean isActive(BotInstance bot, Instant now) {
        return bot.status() == RemoteSystemStatus.ACTIVE
            && bot.availabilityConfirmedUntil().isAfter(now);
    }


    private static String requireId(String botId) {
        if (botId == null || botId.isBlank()) {
            throw new IllegalArgumentException("botId must not be blank");
        }

        return botId.strip();
    }
}
