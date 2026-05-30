package com.magenta.servicios.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderFailedEvent(UUID eventId, UUID orderId, UUID tenantId, String reason, Instant occurredAt) {
    public OrderFailedEvent(UUID orderId, UUID tenantId, String reason) {
        this(UUID.randomUUID(), orderId, tenantId, reason, Instant.now());
    }
}
