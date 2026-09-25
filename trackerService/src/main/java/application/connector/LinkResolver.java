package application.connector;

import application.catalog.model.Link;
import application.catalog.model.ResolvedResource;

public interface LinkResolver {
    ResolvedResource resolve(Link link);
}
