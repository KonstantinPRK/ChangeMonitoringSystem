package application.connector.stackoverflow.api;

/**
 * Передаёт между компонентами данные {@code StackOverflowHttpResponse}.
 */
public record StackOverflowHttpResponse(int statusCode, byte[] body, String contentEncoding) {
}
