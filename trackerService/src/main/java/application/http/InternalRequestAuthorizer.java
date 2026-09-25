package application.http;

import application.config.TrackerProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(1)
public class InternalRequestAuthorizer extends OncePerRequestFilter {
    private final byte[] authorization;
    private final boolean enabled;


    public InternalRequestAuthorizer(TrackerProperties trackerProperties) {
        String token = trackerProperties.internalApiToken();
        authorization = ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
        enabled = !token.isBlank();
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || !request.getRequestURI().startsWith("/api/");
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !authorized(header)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Bearer");
            return;
        }

        chain.doFilter(request, response);
    }


    private boolean authorized(String header) {
        byte[] receivedAuthorization = header.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(authorization, receivedAuthorization);
    }
}
