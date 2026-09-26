package application.connector.github.api;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;

/**
 * Выполняет HTTP-запросы к удалённому сервису для {@code GitHubApiClient}.
 */
@Component
public class GitHubApiClient {
    private final HttpClient httpClient;
    private final GitHubRequestFactory requestFactory;
    private final GitHubHeaderReader headerReader;


    public GitHubApiClient(
            HttpClient httpClient,
            GitHubRequestFactory requestFactory,
            GitHubHeaderReader headerReader
    ) {
        this.httpClient = httpClient;
        this.requestFactory = requestFactory;
        this.headerReader = headerReader;
    }


    public CompletionStage<GitHubHttpResponse> get(String path, String etag) {
        HttpRequest request = requestFactory.get(path, etag);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(headerReader::read);
    }
}
