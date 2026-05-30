package com.magenta.servicios.infrastructure.adapter.out.client;

import com.magenta.servicios.domain.port.out.CustomerClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CustomerRestClient implements CustomerClientPort {

    private final RestClient restClient;

    public CustomerRestClient(@Value("${magenta.clients.customers}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "customers")
    @Retry(name = "customers")
    public CustomerProfile getProfile(UUID customerId) {
        return restClient.get()
                .uri("/api/v1/customers/{id}/profile", customerId)
                .retrieve()
                .body(CustomerProfileResponse.class)
                .toCustomerProfile();
    }

    @Override
    @CircuitBreaker(name = "customers")
    public boolean hasKycApproved(UUID customerId) {
        try {
            CustomerProfile p = getProfile(customerId);
            return p != null && p.kycApproved();
        } catch (Exception e) {
            return false;
        }
    }

    private record CustomerProfileResponse(String id, String fullName, String email,
                                            BigDecimal monthlyIncome, boolean kycApproved) {
        CustomerProfile toCustomerProfile() {
            return new CustomerProfile(UUID.fromString(id), fullName, email, monthlyIncome, kycApproved);
        }
    }
}
