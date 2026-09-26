package application.work;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Описывает сигнал для пробуждения обработчика {@code ScrapeWorkSignal}.
 */
@Component
public class ScrapeWorkSignal {
    private final AtomicReference<Runnable> listener = new AtomicReference<>(() -> { });


    public void connect(Runnable signalListener) {
        listener.set(signalListener);
    }


    public void signal() {
        listener.get().run();
    }
}
