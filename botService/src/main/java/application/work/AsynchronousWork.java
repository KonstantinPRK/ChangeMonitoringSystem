package application.work;

import java.util.concurrent.CompletionStage;

/**
 * Определяет контракт {@code AsynchronousWork}.
 */
@FunctionalInterface
public interface AsynchronousWork {
    /**
     * Обрабатывает следующий доступный элемент, не блокируя вызывающий поток.
     *
     * @return этап со значением {@code true}, если в очереди может оставаться работа
     */
    CompletionStage<Boolean> executeNext();
}
