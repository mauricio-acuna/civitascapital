package com.magenta.products.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Transaction(
        UUID id,
        UUID tenantId,
        UUID propertyId,
        UUID operationId,
        OperationType type,
        BigDecimal finalPrice,
        String currency,
        BigDecimal surfaceSqm,
        BigDecimal pricePerSqm,
        UUID buyerCustomerId,
        UUID sellerCustomerId,
        UUID bankProductId,
        BigDecimal mortgageAmount,
        BigDecimal ltv,
        LocalDate closedAt,
        String deedNotaryProtocol,
        TransactionSource source,
        Instant createdAt) {
}
