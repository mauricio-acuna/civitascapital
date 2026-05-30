package com.magenta.servicios.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderAcceptedEvent(UUID eventId, UUID orderId, UUID tenantId, UUID customerId, Instant occurredAt) {
    public OrderAcceptedEvent(UUID orderId, UUID tenantId, UUID customerId) {
        this(UUID.randomUUID(), orderId, tenantId, customerId, Instant.now());
    }
}
