package application.connector;

import org.springframework.stereotype.Component;

/**
 * Выбирает подходящего получателя запроса через {@code ConnectorRouter}.
 */
@Component
public class ConnectorRouter {
    private final ConnectorRegistry registry;


    public ConnectorRouter(ConnectorRegistry registry) {
        this.registry = registry;
    }


    public TrackingConnector routeHost(String host) {
        return registry.byHost(host);
    }


    public TrackingConnector routeProvider(String providerKey) {
        return registry.byKey(providerKey);
    }
}
