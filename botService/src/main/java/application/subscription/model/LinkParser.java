package application.subscription.model;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class LinkParser {
    public Link parse(String value) {
        URI uri = URI.create(value.trim()).normalize();
        String scheme = uri.getScheme();

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Ссылка должна начинаться с http:// или https://");
        }

        if (uri.getHost() == null) throw new IllegalArgumentException("В ссылке отсутствует домен");

        return new Link(uri.getHost().toLowerCase(), uri.toString());
    }
}
