package com.magenta.servicios.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SlaBreachedEvent(UUID eventId, UUID orderId, UUID tenantId, UUID customerId, Instant slaDueAt, Instant occurredAt) {
    public SlaBreachedEvent(UUID orderId, UUID tenantId, UUID customerId, Instant slaDueAt) {
        this(UUID.randomUUID(), orderId, tenantId, customerId, slaDueAt, Instant.now());
    }
}
