package application.connector.stackoverflow.link;

import application.catalog.model.Link;
import application.catalog.model.ResolvedResource;
import application.connector.LinkResolver;
import application.connector.stackoverflow.StackOverflowProperties;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Преобразует внешний адрес в доменную модель через {@code StackOverflowLinkResolver}.
 */
@Component
public class StackOverflowLinkResolver implements LinkResolver {
    private final StackOverflowProperties properties;


    public StackOverflowLinkResolver(StackOverflowProperties properties) {
        this.properties = properties;
    }


    @Override
    public ResolvedResource resolve(Link link) {
        URI address = URI.create(link.address()).normalize();
        requireSupportedHost(address.getHost());
        StackOverflowTarget target = target(address);
        String canonicalUrl = "https://stackoverflow.com/questions/" + target.questionId();
        return new ResolvedResource(
                "stackoverflow",
                "question",
                canonicalUrl,
                String.valueOf(target.questionId())
        );
    }


    private StackOverflowTarget target(URI address) {
        List<String> segments = Arrays.stream(address.getPath().split("/"))
                .filter(segment -> !segment.isBlank())
                .toList();
        if (segments.size() < 2) throw unsupported(address);
        if (!"questions".equals(segments.get(0)) && !"q".equals(segments.get(0))) {
            throw unsupported(address);
        }

        try {
            long questionId = Long.parseLong(segments.get(1));
            if (questionId <= 0) throw unsupported(address);
            return new StackOverflowTarget(questionId);

        } catch (NumberFormatException exception) {
            throw unsupported(address);

        }
    }


    private void requireSupportedHost(String host) {
        if (host != null && properties.supportedHosts().stream().anyMatch(host::equalsIgnoreCase)) return;
        throw new UnsupportedStackOverflowLinkException("Unsupported StackOverflow host: " + host);
    }


    private UnsupportedStackOverflowLinkException unsupported(URI address) {
        return new UnsupportedStackOverflowLinkException("Unsupported StackOverflow link: " + address);
    }
}
