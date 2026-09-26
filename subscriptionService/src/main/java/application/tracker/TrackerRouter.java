package application.tracker;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Выбирает подходящего получателя запроса через {@code TrackerRouter}.
 */
@Component
public class TrackerRouter {
    private final TrackerRegistry registry;


    public TrackerRouter(TrackerRegistry registry) {
        this.registry = registry;
    }


    public TrackerInstance route(String host) {
        return registry.requireAvailableForHost(host);
    }


    public void requireOwnership(String trackerId, String host) {
        registry.requireOwnership(trackerId, host);
    }


    public List<String> supportedHosts() {
        return registry.supportedHosts();
    }
}
