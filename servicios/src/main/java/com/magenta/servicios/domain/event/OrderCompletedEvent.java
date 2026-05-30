package com.magenta.servicios.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCompletedEvent(UUID eventId, UUID orderId, UUID tenantId, UUID customerId, BigDecimal priceFinal, Instant occurredAt) {
    public OrderCompletedEvent(UUID orderId, UUID tenantId, UUID customerId, BigDecimal priceFinal) {
        this(UUID.randomUUID(), orderId, tenantId, customerId, priceFinal, Instant.now());
    }
}
