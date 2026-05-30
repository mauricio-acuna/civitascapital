package com.magenta.banks.domain.model.loansimulation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root: Simulación de préstamo.
 */
public record LoanSimulation(
    UUID id,
    UUID tenantId,
    UUID customerId,
    UUID productId,
    UUID propertyId,
    UUID zoneId,
    BigDecimal requestedAmount,
    BigDecimal propertyPrice,
    BigDecimal surfaceSqm,
    String propertyType,
    String operationType,
    int termMonths,
    BorrowerProfile borrower,
    TaxInfo taxes,
    SimulationResult result,
    Instant createdAt
) {
    public LoanSimulation {
        if (productId == null) throw new IllegalArgumentException("productId is required");
        if (requestedAmount == null || requestedAmount.signum() <= 0)
            throw new IllegalArgumentException("requestedAmount must be positive");
        if (termMonths <= 0) throw new IllegalArgumentException("termMonths must be positive");
        if (borrower == null) throw new IllegalArgumentException("borrower is required");
    }
}
