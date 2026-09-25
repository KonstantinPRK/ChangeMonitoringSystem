package application.work;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface AsynchronousWork {
    CompletionStage<Boolean> executeNext();
}
