package com.magenta.banks.infrastructure.adapter.out.client;

import com.magenta.banks.domain.port.out.ZoneClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class ZoneRestClient implements ZoneClient {

    private static final Logger log = LoggerFactory.getLogger(ZoneRestClient.class);

    private final RestClient restClient;

    public ZoneRestClient(@Value("${magenta.clients.areas}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    record ZoneInfoResponse(
        UUID zoneId,
        String ccaa,
        BigDecimal ivaPct,
        BigDecimal ajdPct,
        BigDecimal itpPct
    ) {}

    @Override
    @CircuitBreaker(name = "areas")
    public Optional<ZoneInfo> getZoneInfo(UUID zoneId) {
        try {
            ZoneInfoResponse resp = restClient.get()
                    .uri("/api/v1/zones/{id}", zoneId)
                    .retrieve()
                    .body(ZoneInfoResponse.class);

            if (resp == null) return Optional.empty();

            return Optional.of(new ZoneInfo(
                    resp.zoneId(), resp.ccaa(), resp.ivaPct(), resp.ajdPct(), resp.itpPct()));
        } catch (RestClientException e) {
            log.warn("Areas service unavailable for zoneId={}: {}", zoneId, e.getMessage());
            return Optional.empty();
        }
    }
}
