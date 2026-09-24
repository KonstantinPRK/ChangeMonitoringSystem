package application.telegram;

public record TelegramMessage(
    String senderId,
    String chatId,
    String text
) {
}
