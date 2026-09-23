package application;

public record TelegramMessage(String senderId, String chatId, String text) {
}
