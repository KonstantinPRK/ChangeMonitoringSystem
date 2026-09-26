package application.connector.stackoverflow.api;

import application.connector.stackoverflow.StackOverflowProperties;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

/**
 * Формирует запросы внешнего протокола для {@code StackOverflowRequestFactory}.
 */
@Component
public class StackOverflowRequestFactory {
    private final StackOverflowProperties properties;


    public StackOverflowRequestFactory(StackOverflowProperties properties) {
        this.properties = properties;
    }


    public HttpRequest question(long questionId) {
        StringBuilder address = new StringBuilder(properties.apiUrl().toString())
                .append("questions/")
                .append(questionId)
                .append("?site=")
                .append(encode(properties.site()));
        if (properties.key() != null && !properties.key().isBlank()) {
            address.append("&key=").append(encode(properties.key()));
        }
        return HttpRequest.newBuilder(URI.create(address.toString()))
                .timeout(properties.requestTimeout())
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();
    }


    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
