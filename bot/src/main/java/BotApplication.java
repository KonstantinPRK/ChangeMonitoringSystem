
import infrastructure.Client;

import java.util.List;

public final class BotApplication {
    List<Client> clients;


    public void start(){
        for(Client client : clients) client.start();
    }

    public void stop(){
        for(Client client : clients) client.stop();
    }
}
