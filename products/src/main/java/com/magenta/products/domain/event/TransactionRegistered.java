package com.magenta.products.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRegistered(UUID transactionId, UUID propertyId, UUID tenantId,
                                     BigDecimal finalPrice, String currency,
                                     BigDecimal pricePerSqm, Instant occurredAt) {}
