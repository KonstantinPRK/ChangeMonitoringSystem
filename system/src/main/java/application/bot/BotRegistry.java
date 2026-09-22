package application.bot;

import application.registration.ServiceLease;
import java.util.Optional;

public interface BotRegistry {
    void register(BotDescriptor descriptor, ServiceLease lease);

    void unregister(String instanceId);

    Optional<BotDescriptor> findActive(String botId);
}

