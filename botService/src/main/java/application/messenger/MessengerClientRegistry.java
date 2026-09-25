package application.messenger;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessengerClientRegistry {
    private final Map<String, MessengerClient> clientsByBotId;


    public MessengerClientRegistry(List<MessengerClient> clients) {
        Map<String, MessengerClient> registeredClients = new LinkedHashMap<>();
        for (MessengerClient client : clients) {
            MessengerClient occupiedId = registeredClients.putIfAbsent(client.botId(), client);
            if (occupiedId != null) {
                throw new IllegalArgumentException("Duplicate messenger bot ID: " + client.botId());
            }
        }
        clientsByBotId = Map.copyOf(registeredClients);
    }


    public Collection<MessengerClient> clients() {
        return clientsByBotId.values();
    }


    public MessengerClient get(String botId) {
        MessengerClient client = clientsByBotId.get(botId);
        if (client == null) throw new IllegalArgumentException("Unknown messenger bot ID: " + botId);
        return client;
    }


    public boolean contains(String botId) {
        return clientsByBotId.containsKey(botId);
    }
}
