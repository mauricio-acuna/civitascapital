package com.magenta.products.infrastructure.adapter.out.client;

import com.magenta.products.domain.model.GeoPoint;
import com.magenta.products.domain.port.out.ZoneResolverPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class AreasClient implements ZoneResolverPort {

    private static final Logger log = LoggerFactory.getLogger(AreasClient.class);

    private final RestClient restClient;

    public AreasClient(@Value("${magenta.clients.areas.base-url}") String baseUrl,
                        RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "areas", fallbackMethod = "resolveZoneFallback")
    public UUID resolveZone(GeoPoint coordinates) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/zones/resolve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("lat", coordinates.lat(), "lng", coordinates.lng()))
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("zoneId")) {
                return UUID.fromString((String) response.get("zoneId"));
            }
            return null;
        } catch (Exception e) {
            log.warn("Zone resolution failed for {}: {}", coordinates, e.getMessage());
            return null;
        }
    }

    @Override
    @CircuitBreaker(name = "areas", fallbackMethod = "boundaryFallback")
    public boolean isWithinZoneBoundary(GeoPoint coordinates, UUID zoneId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/zones/{id}/boundary/check", zoneId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("lat", coordinates.lat(), "lng", coordinates.lng()))
                    .retrieve()
                    .body(Map.class);

            return response != null && Boolean.TRUE.equals(response.get("within"));
        } catch (Exception e) {
            log.warn("Boundary check failed: {}", e.getMessage());
            return true; // fail open — validation is best-effort
        }
    }

    public UUID resolveZoneFallback(GeoPoint coordinates, Exception ex) {
        log.warn("AreasClient circuit open, cannot resolve zone: {}", ex.getMessage());
        return null;
    }

    public boolean boundaryFallback(GeoPoint coordinates, UUID zoneId, Exception ex) {
        return true;
    }
}
