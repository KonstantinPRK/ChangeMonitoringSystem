package application.connector;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class ConnectorRegistry {
    private final Map<String, TrackingConnector> connectorsByKey;
    private final Map<String, TrackingConnector> connectorsByHost;


    public ConnectorRegistry(List<TrackingConnector> connectors) {
        connectorsByKey = indexByKey(connectors);
        connectorsByHost = indexByHost(connectors);
    }


    public TrackingConnector byKey(String key) {
        TrackingConnector connector = connectorsByKey.get(normalize(key));
        if (connector != null) return connector;
        throw new UnsupportedResourceLinkException("Unknown tracking provider: " + key);
    }


    public TrackingConnector byHost(String host) {
        TrackingConnector connector = connectorsByHost.get(normalize(host));
        if (connector != null) return connector;
        throw new UnsupportedResourceLinkException("Unsupported resource host: " + host);
    }


    public Set<String> supportedHosts() {
        return new TreeSet<>(connectorsByHost.keySet());
    }


    private Map<String, TrackingConnector> indexByKey(List<TrackingConnector> connectors) {
        Map<String, TrackingConnector> index = new HashMap<>();
        for (TrackingConnector connector : connectors) {
            String key = normalize(connector.descriptor().key());
            requireUnoccupied(index, key, connector);
            index.put(key, connector);
        }
        return Map.copyOf(index);
    }


    private Map<String, TrackingConnector> indexByHost(List<TrackingConnector> connectors) {
        Map<String, TrackingConnector> index = new HashMap<>();
        for (TrackingConnector connector : connectors) {
            for (String host : connector.descriptor().supportedHosts()) {
                String normalizedHost = normalize(host);
                requireUnoccupied(index, normalizedHost, connector);
                index.put(normalizedHost, connector);
            }
        }
        return Map.copyOf(index);
    }


    private void requireUnoccupied(
            Map<String, TrackingConnector> index,
            String value,
            TrackingConnector connector
    ) {
        TrackingConnector owner = index.get(value);
        if (owner == null || owner == connector) return;
        throw new IllegalStateException("Connector value is already registered: " + value);
    }


    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
