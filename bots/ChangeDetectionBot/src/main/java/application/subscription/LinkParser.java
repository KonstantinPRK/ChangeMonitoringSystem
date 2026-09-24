package application.subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class LinkParser {
    public Link parse(String text) {
        URI uri;

        try {
            uri = new URI(text.trim()).normalize();

        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(
                "Некорректная ссылка",
                exception
            );

        }

        validate(uri);

        String domain = uri
            .getHost()
            .toLowerCase(Locale.ROOT);

        String resource = extractResource(uri);

        return new Link(
            domain,
            resource
        );
    }


    private void validate(URI uri) {
        String scheme = uri.getScheme();

        boolean supportedScheme =
            "http".equalsIgnoreCase(scheme)
                || "https".equalsIgnoreCase(scheme);

        if (!supportedScheme || uri.getHost() == null) {
            throw new IllegalArgumentException(
                "Некорректная ссылка"
            );
        }
    }


    private String extractResource(URI uri) {
        String resource = uri.getRawPath();

        if (resource == null || resource.isBlank()) {
            resource = "/";
        }

        if (uri.getRawQuery() != null) {
            resource += "?" + uri.getRawQuery();
        }

        return resource;
    }
}
