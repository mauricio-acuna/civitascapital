package com.magenta.servicios.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCapturedEvent(UUID eventId, UUID orderId, UUID tenantId, UUID paymentId, BigDecimal amount, String currency, Instant occurredAt) {
    public PaymentCapturedEvent(UUID orderId, UUID tenantId, UUID paymentId, BigDecimal amount, String currency) {
        this(UUID.randomUUID(), orderId, tenantId, paymentId, amount, currency, Instant.now());
    }
}
