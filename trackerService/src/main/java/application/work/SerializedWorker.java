package application.work;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SerializedWorker {
    private final Executor executor;
    private final AsynchronousWork work;
    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicBoolean requested = new AtomicBoolean();


    public SerializedWorker(Executor executor, AsynchronousWork work) {
        this.executor = executor;
        this.work = work;
    }


    public void signal() {
        requested.set(true);
        startIfIdle();
    }


    private void startIfIdle() {
        if (active.compareAndSet(false, true)) executor.execute(this::executeNext);
    }


    private void executeNext() {
        requested.set(false);

        try {
            work.executeNext().whenComplete(this::complete);

        } catch (RuntimeException exception) {
            complete(false, exception);

        }
    }


    private void complete(Boolean moreWork, Throwable failure) {
        if (failure == null && Boolean.TRUE.equals(moreWork)) requested.set(true);
        active.set(false);
        if (requested.get()) startIfIdle();
    }
}
