package com.magenta.servicios.infrastructure.adapter.out.client;

import com.magenta.servicios.domain.port.out.ProductClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProductRestClient implements ProductClientPort {

    private final RestClient restClient;

    public ProductRestClient(@Value("${magenta.clients.products}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "products")
    @Retry(name = "products")
    public PropertyData getProperty(UUID propertyId) {
        return restClient.get()
                .uri("/api/v1/products/{id}", propertyId)
                .retrieve()
                .body(PropertyResponse.class)
                .toPropertyData();
    }

    private record PropertyResponse(String id, String zoneId, String operationType,
                                     BigDecimal price, String type) {
        PropertyData toPropertyData() {
            return new PropertyData(UUID.fromString(id), UUID.fromString(zoneId),
                    operationType, price, type);
        }
    }
}
