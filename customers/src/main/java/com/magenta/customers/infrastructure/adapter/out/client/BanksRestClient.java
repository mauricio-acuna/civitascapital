package com.magenta.customers.infrastructure.adapter.out.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BanksRestClient {

    @Value("${magenta.clients.banks.url}")
    private String baseUrl;

    private final RestClient.Builder restClientBuilder;

    public record MortgageProductDto(UUID id, String name, BigDecimal tin, BigDecimal ltv) {}

    @CircuitBreaker(name = "banks", fallbackMethod = "productsFallback")
    public java.util.List<MortgageProductDto> getMortgageProducts(UUID tenantId, BigDecimal maxPayment) {
        try {
            return restClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/api/v1/mortgage-products?maxPayment={mp}", maxPayment)
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<java.util.List<MortgageProductDto>>() {});
        } catch (Exception e) {
            log.warn("Banks service unavailable: {}", e.getMessage());
            return java.util.List.of();
        }
    }

    public java.util.List<MortgageProductDto> productsFallback(UUID tenantId, BigDecimal maxPayment, Exception ex) {
        log.warn("Banks circuit breaker open");
        return java.util.List.of();
    }
}
