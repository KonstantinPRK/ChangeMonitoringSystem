package application.connector.stackoverflow.api;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;

@Component
public class StackOverflowApiClient {
    private final HttpClient httpClient;
    private final StackOverflowRequestFactory requestFactory;


    public StackOverflowApiClient(
            HttpClient httpClient,
            StackOverflowRequestFactory requestFactory
    ) {
        this.httpClient = httpClient;
        this.requestFactory = requestFactory;
    }


    public CompletionStage<StackOverflowHttpResponse> question(long questionId) {
        return httpClient.sendAsync(
                requestFactory.question(questionId),
                HttpResponse.BodyHandlers.ofByteArray()
        ).thenApply(response -> new StackOverflowHttpResponse(
                response.statusCode(),
                response.body(),
                response.headers().firstValue("Content-Encoding").orElse("")
        ));
    }
}
