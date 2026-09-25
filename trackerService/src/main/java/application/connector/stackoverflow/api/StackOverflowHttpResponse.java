package application.connector.stackoverflow.api;

public record StackOverflowHttpResponse(int statusCode, byte[] body, String contentEncoding) {
}
