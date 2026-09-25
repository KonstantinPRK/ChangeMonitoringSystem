package application.bot;

import application.queue.QueueReceipt;
import application.subscriptions.SubscriptionRequest;
import application.user.SubscriptionView;
import application.user.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class BotClientApi {
    private final BotManager bots;


    public BotClientApi(BotManager bots) {
        this.bots = bots;
    }


    @PostMapping("/subscriptions/requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public QueueReceipt accept(@Valid @RequestBody SubscriptionRequest request) {
        return bots.saveSubscriptionRequest(request);
    }


    @PostMapping("/subscriptions/requests/batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public QueueReceipt[] acceptBatch(@RequestBody @Size(min = 1, max = 100) List<@NotNull @Valid SubscriptionRequest> requests) {
        return bots.saveSubscriptionRequests(requests.toArray(SubscriptionRequest[]::new));
    }


    @GetMapping("/subscriptions/requests/{requestId}")
    public QueueReceipt status(@PathVariable UUID requestId) {
        return bots.requestStatus(requestId);
    }


    @GetMapping("/subscriptions")
    public List<SubscriptionView> subscriptions(
            @RequestParam @NotBlank @Size(max = 128) String botId,
            @RequestParam @NotBlank @Size(max = 128) String userId,
            @RequestParam @NotBlank @Size(max = 128) String chatId,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limit
    ) {
        return bots.getSubscriptions(new User(botId, userId, chatId), offset, limit);
    }


    @GetMapping("/subscriptions/available")
    public List<String> available() {
        return bots.availableSubscriptions();
    }
}
