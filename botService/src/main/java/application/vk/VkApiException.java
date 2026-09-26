package application.vk;

/**
 * Сигнализирует об ошибке, представленной типом {@code VkApiException}.
 */
public class VkApiException extends RuntimeException {
    private final int errorCode;


    public VkApiException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }


    public int errorCode() {
        return errorCode;
    }
}
