package application.bot;

import org.springframework.stereotype.Component;

@Component
public class BotRouter {
    private final BotRegistry registry;


    public BotRouter(BotRegistry registry) {
        this.registry = registry;
    }


    public BotInstance route(String botId) {
        return registry.requireAvailable(botId);
    }
}
