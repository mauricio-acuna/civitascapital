package com.magenta.banks.infrastructure.adapter.out.client;

import com.magenta.banks.domain.port.out.CustomerClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
public class CustomerRestClient implements CustomerClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerRestClient.class);

    private final RestClient restClient;

    public CustomerRestClient(@Value("${magenta.clients.customers}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    record CustomerFinancialProfileResponse(
        UUID customerId,
        BigDecimal netIncomeMonthly,
        Integer payments,
        Integer age,
        String contractType,
        Integer seniorityMonths,
        BigDecimal otherDebtMonthly,
        Integer dependents,
        Boolean cirbeFlag,
        Boolean kycApproved
    ) {}

    @Override
    @CircuitBreaker(name = "customers", fallbackMethod = "fallbackProfile")
    @Retry(name = "customers")
    public Optional<FinancialProfile> getFinancialProfile(UUID customerId) {
        try {
            CustomerFinancialProfileResponse resp = restClient.get()
                    .uri("/api/v1/customers/{id}/financial-profile", customerId)
                    .retrieve()
                    .body(CustomerFinancialProfileResponse.class);

            if (resp == null) return Optional.empty();

            return Optional.of(new FinancialProfile(
                    resp.customerId(),
                    resp.netIncomeMonthly(),
                    resp.payments() != null ? resp.payments() : 12,
                    resp.age() != null ? resp.age() : 0,
                    resp.contractType(),
                    resp.seniorityMonths() != null ? resp.seniorityMonths() : 0,
                    resp.otherDebtMonthly() != null ? resp.otherDebtMonthly() : BigDecimal.ZERO,
                    resp.dependents() != null ? resp.dependents() : 0,
                    Boolean.TRUE.equals(resp.cirbeFlag()),
                    Boolean.TRUE.equals(resp.kycApproved())));
        } catch (RestClientException e) {
            log.warn("Customer service unavailable for customerId={}: {}", customerId, e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    public Optional<FinancialProfile> fallbackProfile(UUID customerId, Throwable t) {
        log.error("Circuit breaker open for customers. customerId={}", customerId, t);
        return Optional.empty();
    }
}
