package application.tracker;

import java.net.URI;

public interface TrackerRouter {
    TrackerDescriptor route(URI resource);
}

