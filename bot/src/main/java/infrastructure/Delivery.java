package infrastructure;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Delivery<T> {

    private final T message;
    private final Runnable acknowledgeAction;
    private final Runnable retryAction;
    private final AtomicBoolean completed = new AtomicBoolean();

    public Delivery(T message, Runnable acknowledgeAction, Runnable retryAction) {
        this.message = message;
        this.acknowledgeAction = acknowledgeAction;
        this.retryAction = retryAction;
    }

    public T message() {
        return message;
    }

    public void acknowledge() {
        if (completed.compareAndSet(false, true)) {
            acknowledgeAction.run();
        }
    }

    public void retry() {
        if (completed.compareAndSet(false, true)) {
            retryAction.run();
        }
    }
}
