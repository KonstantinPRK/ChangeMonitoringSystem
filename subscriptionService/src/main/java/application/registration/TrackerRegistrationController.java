package application.registration;

import application.tracker.TrackerInstance;
import application.tracker.TrackerRegistry;

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
@RequestMapping("/internal/trackers")
public class TrackerRegistrationController {
    private final TrackerRegistry registry;


    public TrackerRegistrationController(TrackerRegistry registry) {
        this.registry = registry;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@Valid @RequestBody TrackerRegistrationRequest request) {
        registry.register(request.trackerId(), request.baseUrl(), request.supportedHosts());
    }


    @GetMapping
    public List<TrackerInstance> list() {
        return registry.list();
    }


    @PutMapping("/{trackerId}/availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmAvailability(@PathVariable("trackerId") String trackerId) {
        registry.confirmAvailability(trackerId);
    }


    @DeleteMapping("/{trackerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable("trackerId") String trackerId) {
        registry.remove(trackerId);
    }
}
