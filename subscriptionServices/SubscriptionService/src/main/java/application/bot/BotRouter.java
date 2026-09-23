package application.bot;

import java.time.Clock;

public final class BotRouter {
    private final BotRegistry registry;
    private final Clock clock;


    public BotRouter(BotRegistry registry, Clock clock) {
        this.registry = registry;
        this.clock = clock;
    }


    public BotInstance route(String botId) {
        return registry.findActive(botId, clock.instant())
            .orElseThrow(() -> new IllegalStateException("Bot is unavailable: " + botId));
    }
}
