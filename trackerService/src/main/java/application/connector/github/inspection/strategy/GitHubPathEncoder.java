package application.connector.github.inspection.strategy;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Реализует ответственность компонента {@code GitHubPathEncoder}.
 */
@Component
public class GitHubPathEncoder {
    public String encode(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
