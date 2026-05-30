package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@With
public class FinancialSnapshot {

    private final UUID id;
    private final UUID customerId;
    private final LocalDate asOf;
    private final BigDecimal netIncomeMonthly;
    private final int payments;                  // 12 ó 14
    private final BigDecimal grossIncomeAnnual;
    private final BigDecimal otherDebtMonthly;
    private final boolean cirbeFlag;
    private final BigDecimal ownFunds;
    private final int existingProperties;
    private final BigDecimal rentalIncomeMonthly;
    /** 0..1 calculado por ConfidenceCalculator */
    private final BigDecimal confidence;
    private final ComputedAffordability computed;
}
