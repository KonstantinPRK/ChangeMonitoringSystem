package application.connector.github.link;

import application.connector.github.model.GitHubResourceType;
import application.connector.github.model.GitHubTarget;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
public class GitHubLinkParser {
    public GitHubTarget parse(String address) {
        URI uri = URI.create(address).normalize();
        requireGitHubHost(uri.getHost());
        List<String> segments = Arrays.stream(uri.getPath().split("/"))
                .filter(segment -> !segment.isBlank())
                .toList();
        if (segments.size() == 2) return repository(segments);
        if (segments.size() == 4 && "issues".equals(segments.get(2))) return issue(segments);
        if (segments.size() == 4 && "pull".equals(segments.get(2))) return pullRequest(segments);
        throw new UnsupportedGitHubLinkException("Unsupported GitHub link: " + address);
    }


    private GitHubTarget repository(List<String> segments) {
        return new GitHubTarget(
                GitHubResourceType.REPOSITORY,
                segments.get(0),
                removeGitSuffix(segments.get(1)),
                null
        );
    }


    private GitHubTarget issue(List<String> segments) {
        return numberedResource(GitHubResourceType.ISSUE, segments);
    }


    private GitHubTarget pullRequest(List<String> segments) {
        return numberedResource(GitHubResourceType.PULL_REQUEST, segments);
    }


    private GitHubTarget numberedResource(GitHubResourceType type, List<String> segments) {
        try {
            return new GitHubTarget(
                    type,
                    segments.get(0),
                    removeGitSuffix(segments.get(1)),
                    Long.valueOf(segments.get(3))
            );

        } catch (NumberFormatException exception) {
            throw new UnsupportedGitHubLinkException("GitHub resource number is invalid");

        }
    }


    private void requireGitHubHost(String host) {
        if ("github.com".equalsIgnoreCase(host) || "www.github.com".equalsIgnoreCase(host)) return;
        throw new UnsupportedGitHubLinkException("Unsupported GitHub host: " + host);
    }


    private String removeGitSuffix(String repository) {
        if (repository.endsWith(".git")) return repository.substring(0, repository.length() - 4);
        return repository;
    }
}
