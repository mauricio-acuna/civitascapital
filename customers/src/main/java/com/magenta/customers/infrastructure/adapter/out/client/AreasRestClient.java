package com.magenta.customers.infrastructure.adapter.out.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Cliente HTTP hacia el módulo `areas`.
 * Cabeceras requeridas por ARCHITECTURE.md § 5.8: X-Tenant-Id, X-Request-Id, traceparent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AreasRestClient {

    @Value("${magenta.clients.areas.url}")
    private String baseUrl;

    private final RestClient.Builder restClientBuilder;

    public record ZoneDto(UUID id, String name, String parentId) {}

    @CircuitBreaker(name = "areas", fallbackMethod = "zoneFallback")
    public Optional<ZoneDto> getZone(UUID zoneId, UUID tenantId) {
        try {
            ZoneDto zone = restClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/api/v1/zones/{id}", zoneId)
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(ZoneDto.class);
            return Optional.ofNullable(zone);
        } catch (Exception e) {
            log.warn("Areas service unavailable: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ZoneDto> zoneFallback(UUID zoneId, UUID tenantId, Exception ex) {
        log.warn("Areas circuit breaker open for zone={}", zoneId);
        return Optional.empty();
    }
}
