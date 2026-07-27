package com.stayseat.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory per-IP token bucket. The report (4.7) only asks for a
 * "simple bucket, doesn't need to be fancy" - this avoids a Redis dependency
 * while still shedding abusive clients with 429 Too Many Requests. For a real
 * multi-instance deployment this would move to Redis (RequestRateLimiter).
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final int capacity;
    private final double refillPerSecond;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.ratelimit.capacity:100}") int capacity,
            @Value("${app.ratelimit.refill-per-second:50}") double refillPerSecond) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = clientIp(exchange);
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> new Bucket(capacity));
        if (!bucket.tryConsume(capacity, refillPerSecond)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private String clientIp(ServerWebExchange exchange) {
        var remote = exchange.getRequest().getRemoteAddress();
        return (remote == null || remote.getAddress() == null)
                ? "unknown"
                : remote.getAddress().getHostAddress();
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillNanos;

        Bucket(int initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume(int capacity, double refillPerSecond) {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
            tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
            lastRefillNanos = now;
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    @Override
    public int getOrder() {
        return -200; // run first, before auth
    }
}
