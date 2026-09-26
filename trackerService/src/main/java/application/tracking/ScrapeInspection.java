package application.tracking;

import application.snapshot.ResourceObservation;

/**
 * Передаёт между компонентами данные {@code ScrapeInspection}.
 */
public record ScrapeInspection(boolean deferred, ResourceObservation observation) {
    public static ScrapeInspection deferredInspection() {
        return new ScrapeInspection(true, null);
    }


    public static ScrapeInspection observed(ResourceObservation observation) {
        return new ScrapeInspection(false, observation);
    }
}
