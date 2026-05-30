package com.magenta.servicios.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(UUID eventId, UUID orderId, UUID tenantId, String reason, Instant occurredAt) {
    public OrderCancelledEvent(UUID orderId, UUID tenantId, String reason) {
        this(UUID.randomUUID(), orderId, tenantId, reason, Instant.now());
    }
}
