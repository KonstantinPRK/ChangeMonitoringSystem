package application.telegram;

import java.time.Duration;

/**
 * Сигнализирует об ошибке, представленной типом {@code TelegramApiException}.
 */
public class TelegramApiException extends RuntimeException {
    private final int errorCode;
    private final Duration retryAfter;


    public TelegramApiException(int errorCode, String description, Duration retryAfter) {
        super(description);
        this.errorCode = errorCode;
        this.retryAfter = retryAfter;
    }


    public int errorCode() {
        return errorCode;
    }


    public Duration retryAfter() {
        return retryAfter;
    }
}
