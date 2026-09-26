package application.connector.github.api;

import application.connector.github.GitHubProperties;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * Формирует запросы внешнего протокола для {@code GitHubRequestFactory}.
 */
@Component
public class GitHubRequestFactory {
    private final GitHubProperties gitHubProperties;


    public GitHubRequestFactory(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
    }


    public HttpRequest get(String path, String etag) {
        URI address = gitHubProperties.apiUrl().resolve(path);
        HttpRequest.Builder request = HttpRequest.newBuilder(address)
                .timeout(gitHubProperties.requestTimeout())
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", gitHubProperties.apiVersion())
                .header("User-Agent", "ChangeMonitoringSystem-TrackerService")
                .GET();
        String token = gitHubProperties.token();
        if (token != null && !token.isBlank()) request.header("Authorization", "Bearer " + token);
        if (etag != null && !etag.isBlank()) request.header("If-None-Match", etag);
        return request.build();
    }
}
