package com.magenta.servicios.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayoutSentEvent(UUID eventId, UUID orderId, UUID tenantId, UUID partnerId, BigDecimal amount, Instant occurredAt) {
    public PayoutSentEvent(UUID orderId, UUID tenantId, UUID partnerId, BigDecimal amount) {
        this(UUID.randomUUID(), orderId, tenantId, partnerId, amount, Instant.now());
    }
}
