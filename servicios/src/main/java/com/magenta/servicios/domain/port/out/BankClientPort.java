package com.magenta.servicios.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankClientPort {
    FinancingFeasibility getFinancingFeasibility(UUID customerId, UUID propertyId);
    String createPreapproval(UUID customerId, UUID propertyId, BigDecimal amount);

    record FinancingFeasibility(boolean feasible, BigDecimal maxAmount,
                                BigDecimal effortRatio, boolean qualifiesFor905) {}
}
