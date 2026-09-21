package application.client;

import application.messenger.SubscriberId;
import java.net.URI;
import java.util.List;

public interface TrackerClient {
    /** Registers the subscriber; repeating registration must not create a duplicate. */
    void register(SubscriberId subscriberId);

    List<URI> listLinks(SubscriberId subscriberId);
}
