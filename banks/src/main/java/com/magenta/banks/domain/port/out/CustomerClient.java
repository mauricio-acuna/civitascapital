package com.magenta.banks.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto outbound hacia el módulo customers.
 */
public interface CustomerClient {

    record FinancialProfile(
        UUID customerId,
        BigDecimal netIncomeMonthly,
        int payments,
        int age,
        String contractType,
        int seniorityMonths,
        BigDecimal otherDebtMonthly,
        int dependents,
        boolean cirbeFlag,
        boolean kycApproved
    ) {}

    Optional<FinancialProfile> getFinancialProfile(UUID customerId);
}
