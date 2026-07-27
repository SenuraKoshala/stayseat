package com.stayseat.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * First-line JWT check for every request except the public auth endpoints and
 * the payment webhook (contract 4.7). On success it forwards the caller's
 * identity to downstream services as {@code X-User-Id} / {@code X-User-Role}
 * headers, which they trust (see each service's JwtAuthFilter). Any client-
 * supplied identity headers are stripped first so they cannot be spoofed.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /** Paths reachable without a token (prefix match). */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/payments/webhook"
    );

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Never let a client set these directly - only this filter may.
        ServerHttpRequest sanitized = request.mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Role");
                })
                .build();

        if (isPublic(path)) {
            return chain.filter(exchange.mutate().request(sanitized).build());
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or malformed Authorization header.");
        }

        String token = authHeader.substring("Bearer ".length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            UUID.fromString(userId); // sanity-check the subject is a UUID

            ServerHttpRequest authenticated = sanitized.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role == null ? "" : role)
                    .build();

            return chain.filter(exchange.mutate().request(authenticated).build());
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "Invalid or expired token.");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"success\":false,\"error\":{\"code\":\"UNAUTHENTICATED\",\"message\":\""
                + message + "\"}}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // after rate limiting, before routing
    }
}
