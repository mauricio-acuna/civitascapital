package com.magenta.products.infrastructure.adapter.out.client;

import com.magenta.products.domain.model.FinancingHint;
import com.magenta.products.domain.port.out.FinancingPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BanksClient implements FinancingPort {

    private static final Logger log = LoggerFactory.getLogger(BanksClient.class);

    private final RestClient restClient;

    public BanksClient(@Value("${magenta.clients.banks.base-url}") String baseUrl,
                        RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "banks", fallbackMethod = "evaluateFallback")
    public FinancingHint evaluateFeasibility(UUID propertyId, BigDecimal price) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/financing-feasibility")
                            .queryParam("propertyId", propertyId)
                            .queryParam("price", price)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) return FinancingHint.empty();

            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) response.getOrDefault("feasibleBankProductIds", List.of());
            String best = (String) response.get("best90_5_5ProductId");

            return new FinancingHint(
                    ids.stream().map(UUID::fromString).collect(Collectors.toSet()),
                    best != null ? UUID.fromString(best) : null,
                    java.time.Instant.now());
        } catch (Exception e) {
            log.warn("Financing evaluation failed for property {}: {}", propertyId, e.getMessage());
            return FinancingHint.empty();
        }
    }

    public FinancingHint evaluateFallback(UUID propertyId, BigDecimal price, Exception ex) {
        log.warn("BanksClient circuit open: {}", ex.getMessage());
        return FinancingHint.empty();
    }
}
