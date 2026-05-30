package com.magenta.servicios.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundedEvent(UUID eventId, UUID orderId, UUID tenantId, UUID paymentId, BigDecimal amount, Instant occurredAt) {
    public PaymentRefundedEvent(UUID orderId, UUID tenantId, UUID paymentId, BigDecimal amount) {
        this(UUID.randomUUID(), orderId, tenantId, paymentId, amount, Instant.now());
    }
}
