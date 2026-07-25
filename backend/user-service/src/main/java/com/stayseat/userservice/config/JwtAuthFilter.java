package com.stayseat.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Two ways a request can be authenticated:
 *  1. Behind the API Gateway: it already validated the JWT and forwards
 *     X-User-Id / X-User-Role headers (contract section 4.7). We trust those.
 *  2. Direct calls during local dev/testing: we parse and verify the
 *     Authorization: Bearer &lt;jwt&gt; ourselves using the shared secret.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, java.io.IOException {

        CurrentUser currentUser = fromGatewayHeaders(request);
        if (currentUser == null) {
            currentUser = fromBearerToken(request);
        }

        if (currentUser != null) {
            SecurityContextHolder.getContext().setAuthentication(new CurrentUserAuthentication(currentUser));
        }

        chain.doFilter(request, response);
    }

    private CurrentUser fromGatewayHeaders(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        if (userId == null || role == null) {
            return null;
        }
        try {
            return new CurrentUser(UUID.fromString(userId), role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private CurrentUser fromBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring("Bearer ".length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);
            return new CurrentUser(userId, role);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
