package application.tracker;

import application.link.TrackedLink;
import application.persistence.LinkRepository;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Реализует ответственность компонента {@code TrackerLinkApi}.
 */
@RestController
@Validated
public class TrackerLinkApi {
    private final TrackerRegistry trackers;
    private final LinkRepository links;


    public TrackerLinkApi(TrackerRegistry trackers, LinkRepository links) {
        this.trackers = trackers;
        this.links = links;
    }


    @GetMapping("/api/v1/trackers/{trackerId}/links")
    public List<TrackedLink> activeLinks(
            @PathVariable String trackerId,
            @RequestParam(defaultValue = "0") @Min(0) long afterId,
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limit
    ) {
        TrackerInstance tracker = trackers.requireAvailable(trackerId);
        return links.activeLinks(tracker.supportedHosts(), afterId, limit);
    }
}
