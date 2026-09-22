package application.tracker;

import application.registration.ServiceLease;
import java.util.Optional;

public interface TrackerRegistry {
    void register(TrackerDescriptor descriptor, ServiceLease lease);

    void unregister(String instanceId);

    Optional<TrackerDescriptor> findBySupportedHost(String host);
}

