package application.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(1)
public class InternalApiAuthentication extends OncePerRequestFilter {
    private final byte[] authorization;
    private final boolean enabled;


    public InternalApiAuthentication(@Value("${app.internal-api-token:}") String token) {
        this.authorization = ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
        this.enabled = !token.isBlank();
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !enabled || (!path.startsWith("/api/") && !path.startsWith("/internal/"));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !MessageDigest.isEqual(authorization, header.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Bearer");
            return;
        }
        chain.doFilter(request, response);
    }
}
