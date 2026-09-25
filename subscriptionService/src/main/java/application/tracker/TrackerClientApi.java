package application.tracker;

import application.link.LinkNotification;
import application.queue.QueueReceipt;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/updates")
public class TrackerClientApi {
    private final TrackerManager trackers;


    public TrackerClientApi(TrackerManager trackers) {
        this.trackers = trackers;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public QueueReceipt accept(@Valid @RequestBody LinkNotification notification) {
        return trackers.putLinkNotification(notification);
    }


    @GetMapping("/{eventId}")
    public QueueReceipt status(@PathVariable UUID eventId) {
        return trackers.notificationStatus(eventId);
    }
}
