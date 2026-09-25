package application.notification.creation;

import application.snapshot.ResourceSnapshot;
import application.catalog.model.TrackedResource;

import org.springframework.stereotype.Component;

@Component
public class NotificationMessageFactory {
    public String create(TrackedResource resource, ResourceSnapshot snapshot) {
        String state = snapshot.state() == null ? "" : " (state: " + snapshot.state() + ')';
        return resource.providerKey() + ' ' + resource.resourceKind()
                + " was updated: " + resource.address() + state;
    }
}
