package application.connector.github.link;

import application.catalog.model.Link;
import application.catalog.model.ResolvedResource;
import application.connector.LinkResolver;
import application.connector.github.model.GitHubTarget;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GitHubLinkResolver implements LinkResolver {
    private final GitHubLinkParser linkParser;


    public GitHubLinkResolver(GitHubLinkParser linkParser) {
        this.linkParser = linkParser;
    }


    @Override
    public ResolvedResource resolve(Link link) {
        GitHubTarget target = linkParser.parse(link.address());
        String repository = target.owner() + '/' + target.repository();
        String remoteKey = switch (target.type()) {
            case REPOSITORY -> repository;
            case ISSUE -> repository + "/issues/" + target.resourceNumber();
            case PULL_REQUEST -> repository + "/pull/" + target.resourceNumber();
        };
        return new ResolvedResource(
                "github",
                target.type().name().toLowerCase(Locale.ROOT),
                "https://github.com/" + remoteKey,
                remoteKey
        );
    }
}
