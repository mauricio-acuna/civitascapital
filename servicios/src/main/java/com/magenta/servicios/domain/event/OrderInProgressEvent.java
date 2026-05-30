package com.magenta.servicios.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderInProgressEvent(UUID eventId, UUID orderId, UUID tenantId, String workflowInstanceId, Instant occurredAt) {
    public OrderInProgressEvent(UUID orderId, UUID tenantId, String workflowInstanceId) {
        this(UUID.randomUUID(), orderId, tenantId, workflowInstanceId, Instant.now());
    }
}
