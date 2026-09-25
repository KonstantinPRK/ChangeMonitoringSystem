package application.snapshot;

import org.springframework.stereotype.Component;

@Component
public class ChangeDetector {
    public boolean changed(StoredSnapshot previous, ResourceSnapshot current) {
        return !previous.snapshot().contentHash().equals(current.contentHash());
    }
}
