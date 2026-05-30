package com.magenta.areas.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting con Bucket4j en memoria (por IP).
 * - /zones/search   → 60 rpm / IP
 * - /compare        → 30 rpm / IP
 *
 * En producción los buckets deben almacenarse en Redis para escalar horizontalmente.
 */
@Component
public class RateLimitConfig extends OncePerRequestFilter {

    // Buckets por IP para cada endpoint limitado
    private final Map<String, Bucket> searchBuckets  = new ConcurrentHashMap<>();
    private final Map<String, Bucket> compareBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip   = getClientIp(request);

        Bucket bucket = null;
        if (path.startsWith("/api/v1/zones/search")) {
            bucket = searchBuckets.computeIfAbsent(ip, k -> buildBucket(60));
        } else if (path.startsWith("/api/v1/compare")) {
            bucket = compareBuckets.computeIfAbsent(ip, k -> buildBucket(30));
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"status\":429,\"title\":\"Too Many Requests\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private Bucket buildBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }
}
