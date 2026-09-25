package application.registration;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.IDN;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class RegistrationValidator {
    public String identifier(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service ID must contain 1–128 letters, digits, dots, underscores or hyphens"
            );
        }
        return identifier;
    }


    public URI baseUrl(URI baseUrl) {
        if (baseUrl == null || baseUrl.getHost() == null || !isHttp(baseUrl.getScheme())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service baseUrl must be an absolute HTTP(S) URL");
        }
        if (baseUrl.getUserInfo() != null || baseUrl.getQuery() != null || baseUrl.getFragment() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service baseUrl cannot contain credentials, a query or a fragment");
        }
        if (baseUrl.getPort() == 0 || baseUrl.getPort() > 65535) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service port must be between 1 and 65535");
        }
        String address = baseUrl.normalize().toASCIIString();
        return URI.create(address.endsWith("/") ? address : address + "/");
    }


    public String host(String host) {
        if (host == null || host.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host is required");
        try {
            String normalized = IDN.toASCII(host.strip(), IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
            if (normalized.endsWith(".")) normalized = normalized.substring(0, normalized.length() - 1);
            if (normalized.isBlank() || normalized.length() > 253 || normalized.contains("..")) {
                throw new IllegalArgumentException("Invalid host");
            }
            return normalized;

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host must be a domain name without a scheme, port or path");

        }
    }


    public Set<String> hosts(Set<String> hosts) {
        if (hosts == null || hosts.isEmpty() || hosts.size() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supportedHosts must contain between 1 and 100 hosts");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String host : hosts) normalized.add(host(host));
        return Set.copyOf(normalized);
    }


    private boolean isHttp(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }
}
