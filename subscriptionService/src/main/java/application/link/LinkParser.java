package application.link;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Читает и преобразует входные данные для {@code LinkParser}.
 */
@Component
public class LinkParser {
    private static final int MAXIMUM_ADDRESS_LENGTH = 2048;


    public Link parse(String address) {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Link address is required");
        if (address.length() > MAXIMUM_ADDRESS_LENGTH) throw new IllegalArgumentException("Link address is too long");

        try {
            URI uri = URI.create(new URI(address.trim()).normalize().toASCIIString());
            validate(uri);
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String domain = uri.getHost().toLowerCase(Locale.ROOT);
            String authority = normalizeAuthority(domain, uri.getPort(), scheme);
            String path = uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
            String normalized = scheme + "://" + authority + path + query;
            if (normalized.length() > MAXIMUM_ADDRESS_LENGTH) throw new IllegalArgumentException("Normalized link address is too long");
            return new Link(domain, normalized);

        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Link address must be a valid HTTP or HTTPS URL", exception);

        }
    }


    public Link normalize(Link link) {
        if (link == null) throw new IllegalArgumentException("Link is required");
        Link normalized = parse(link.address());
        if (!normalized.domain().equalsIgnoreCase(link.domain())) {
            throw new IllegalArgumentException("Link domain must match the URL host");
        }
        return normalized;
    }


    private void validate(URI uri) {
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only HTTP and HTTPS links are supported");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) throw new IllegalArgumentException("Link host is required");
        if (uri.getHost().length() > 253) throw new IllegalArgumentException("Link host is too long");
        if (uri.getRawUserInfo() != null) throw new IllegalArgumentException("Links must not contain credentials");
        if (uri.getPort() > 65535) throw new IllegalArgumentException("Link port is invalid");
    }


    private String normalizeAuthority(String domain, int port, String scheme) {
        boolean defaultPort = port == -1 || (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
        return defaultPort ? domain : domain + ":" + port;
    }
}
