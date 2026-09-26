package application.snapshot;

import org.springframework.stereotype.Component;

/**
 * Реализует ответственность компонента {@code ChangeDetector}.
 */
@Component
public class ChangeDetector {
    public boolean changed(StoredSnapshot previous, ResourceSnapshot current) {
        return !previous.snapshot().contentHash().equals(current.contentHash());
    }
}
