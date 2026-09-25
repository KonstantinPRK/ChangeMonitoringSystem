package application.registration;

import application.bot.BotInstance;
import application.bot.BotRegistry;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/bots")
public class BotRegistrationController {
    private final BotRegistry registry;


    public BotRegistrationController(BotRegistry registry) {
        this.registry = registry;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@Valid @RequestBody BotRegistrationRequest request) {
        registry.register(request.botId(), request.baseUrl());
    }


    @GetMapping
    public List<BotInstance> list() {
        return registry.list();
    }


    @PutMapping("/{botId}/availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmAvailability(@PathVariable("botId") String botId) {
        registry.confirmAvailability(botId);
    }


    @DeleteMapping("/{botId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable("botId") String botId) {
        registry.remove(botId);
    }
}
