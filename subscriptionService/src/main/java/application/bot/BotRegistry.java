package application.bot;

import application.registration.RegistrationAvailabilityPolicy;
import application.registration.RegistrationValidator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotRegistry {
    private final Map<String, BotInstance> botsById = new HashMap<>();
    private final RegistrationValidator validator;
    private final RegistrationAvailabilityPolicy availabilityPolicy;


    public BotRegistry(
        RegistrationValidator validator,
        Clock clock,
        @Value("${app.registration.availability-timeout:30s}")
        Duration availabilityTimeout
    ) {
        this.validator = validator;
        this.availabilityPolicy = new RegistrationAvailabilityPolicy(
            clock,
            availabilityTimeout
        );
    }


    public synchronized BotInstance register(String botId, URI baseUrl) {
        BotInstance bot = createValidatedBot(botId, baseUrl);

        botsById.put(bot.botId(), bot);

        return bot;
    }


    public synchronized BotInstance confirmAvailability(String botId) {
        BotInstance registeredBot = requireRegistered(botId);
        BotInstance availableBot = new BotInstance(
            registeredBot.botId(),
            registeredBot.baseUrl(),
            availabilityPolicy.nextDeadline()
        );

        botsById.put(botId, availableBot);

        return availableBot;
    }


    public synchronized BotInstance requireAvailable(String botId) {
        BotInstance bot = requireRegistered(botId);
        availabilityPolicy.requireAvailable(
            bot.availableUntil(),
            "Bot"
        );

        return bot;
    }


    public synchronized List<BotInstance> list() {
        List<BotInstance> registeredBots =
            new ArrayList<>(botsById.values());

        registeredBots.sort(
            Comparator.comparing(BotInstance::botId)
        );

        return List.copyOf(registeredBots);
    }


    public synchronized void remove(String botId) {
        botsById.remove(validator.identifier(botId));
    }


    private BotInstance createValidatedBot(String botId, URI baseUrl) {
        String normalizedBotId = validator.identifier(botId);
        URI normalizedBaseUrl = validator.baseUrl(baseUrl);

        return new BotInstance(
            normalizedBotId,
            normalizedBaseUrl,
            availabilityPolicy.nextDeadline()
        );
    }


    private BotInstance requireRegistered(String botId) {
        BotInstance bot = botsById.get(validator.identifier(botId));

        if (bot == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Bot is not registered"
            );
        }

        return bot;
    }
}
