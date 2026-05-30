package com.magenta.banks.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SimulationRequest(
    @NotNull UUID productId,
    UUID propertyId,
    UUID zoneId,
    @NotNull @Positive BigDecimal requestedAmount,
    BigDecimal propertyPrice,
    BigDecimal surfaceSqm,
    String propertyType,
    String operationType,
    @NotNull @Min(12) int termMonths,
    @NotNull BorrowerRequest borrower
) {
    public record BorrowerRequest(
        @NotNull @Positive BigDecimal netIncomeMonthly,
        int payments,
        @Min(18) int age,
        @NotNull String contractType,
        int seniorityMonths,
        BigDecimal otherDebtMonthly,
        int dependents,
        BigDecimal ownFunds,
        boolean hasGuarantor
    ) {}
}
