package com.magenta.servicios.infrastructure.adapter.out.client;

import com.magenta.servicios.domain.port.out.BankClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class BankRestClient implements BankClientPort {

    private final RestClient restClient;

    public BankRestClient(@Value("${magenta.clients.banks}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "banks")
    public FinancingFeasibility getFinancingFeasibility(UUID customerId, UUID propertyId) {
        return restClient.get()
                .uri("/api/v1/financing-feasibility?customerId={c}&propertyId={p}",
                        customerId, propertyId)
                .retrieve()
                .body(FinancingFeasibilityResponse.class)
                .toFeasibility();
    }

    @Override
    @CircuitBreaker(name = "banks")
    public String createPreapproval(UUID customerId, UUID propertyId, BigDecimal amount) {
        return restClient.post()
                .uri("/api/v1/preapprovals")
                .body(new PreapprovalRequest(customerId.toString(), propertyId.toString(), amount))
                .retrieve()
                .body(PreapprovalResponse.class)
                .ref();
    }

    private record FinancingFeasibilityResponse(boolean feasible, BigDecimal maxAmount,
                                                 BigDecimal effortRatio, boolean qualifiesFor905) {
        FinancingFeasibility toFeasibility() {
            return new FinancingFeasibility(feasible, maxAmount, effortRatio, qualifiesFor905);
        }
    }

    private record PreapprovalRequest(String customerId, String propertyId, BigDecimal amount) {}
    private record PreapprovalResponse(String ref) {}
}
