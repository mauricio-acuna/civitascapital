package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.FinancingHint;

import java.util.UUID;

public interface FinancingPort {
    /**
     * Queries the banks module for financing feasibility.
     * Returns an updated FinancingHint (may be empty if no products match).
     */
    FinancingHint evaluateFeasibility(UUID propertyId, java.math.BigDecimal price);
}
