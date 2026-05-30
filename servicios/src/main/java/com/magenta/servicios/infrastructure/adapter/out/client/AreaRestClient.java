package com.magenta.servicios.infrastructure.adapter.out.client;

import com.magenta.servicios.domain.port.out.AreaClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class AreaRestClient implements AreaClientPort {

    private final RestClient restClient;

    public AreaRestClient(@Value("${magenta.clients.areas}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "areas")
    public ZoneData getZone(UUID zoneId) {
        return restClient.get()
                .uri("/api/v1/zones/{id}", zoneId)
                .retrieve()
                .body(ZoneResponse.class)
                .toZoneData();
    }

    @Override
    @CircuitBreaker(name = "areas")
    public boolean isAncestorOf(UUID parentZoneId, UUID childZoneId) {
        try {
            Boolean result = restClient.get()
                    .uri("/api/v1/zones/{parent}/is-ancestor-of/{child}", parentZoneId, childZoneId)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    private record ZoneResponse(String id, String name, String level, String parentId) {
        ZoneData toZoneData() {
            return new ZoneData(UUID.fromString(id), name, level,
                    parentId != null ? UUID.fromString(parentId) : null);
        }
    }
}
