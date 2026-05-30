package com.magenta.servicios.domain.event;

import com.magenta.servicios.domain.model.ServiceCode;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        UUID tenantId,
        UUID customerId,
        ServiceCode serviceCode,
        Instant occurredAt
) {
    public OrderCreatedEvent(UUID orderId, UUID tenantId, UUID customerId, ServiceCode serviceCode) {
        this(UUID.randomUUID(), orderId, tenantId, customerId, serviceCode, Instant.now());
    }
}
