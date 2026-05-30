package com.magenta.banks.infrastructure.adapter.out.client;

import com.magenta.banks.domain.port.out.PropertyClient;
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
public class PropertyRestClient implements PropertyClient {

    private static final Logger log = LoggerFactory.getLogger(PropertyRestClient.class);

    private final RestClient restClient;

    public PropertyRestClient(@Value("${magenta.clients.products}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    record PropertyInfoResponse(
        UUID propertyId,
        BigDecimal price,
        BigDecimal surfaceSqm,
        String type,
        String operationType,
        UUID zoneId
    ) {}

    @Override
    @CircuitBreaker(name = "products")
    public Optional<PropertyInfo> getPropertyInfo(UUID propertyId) {
        try {
            PropertyInfoResponse resp = restClient.get()
                    .uri("/api/v1/properties/{id}", propertyId)
                    .retrieve()
                    .body(PropertyInfoResponse.class);

            if (resp == null) return Optional.empty();

            return Optional.of(new PropertyInfo(
                    resp.propertyId(), resp.price(), resp.surfaceSqm(),
                    resp.type(), resp.operationType(), resp.zoneId()));
        } catch (RestClientException e) {
            log.warn("Products service unavailable for propertyId={}: {}", propertyId, e.getMessage());
            return Optional.empty();
        }
    }
}
