package com.magenta.servicios.domain.event;

import com.magenta.servicios.domain.model.DeliverableKind;

import java.time.Instant;
import java.util.UUID;

public record DeliverableIssuedEvent(UUID eventId, UUID orderId, UUID tenantId, UUID deliverableId, DeliverableKind kind, Instant occurredAt) {
    public DeliverableIssuedEvent(UUID orderId, UUID tenantId, UUID deliverableId, DeliverableKind kind) {
        this(UUID.randomUUID(), orderId, tenantId, deliverableId, kind, Instant.now());
    }
}
