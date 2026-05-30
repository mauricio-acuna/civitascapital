package com.magenta.banks.domain.model.loansimulation;

import com.magenta.banks.domain.model.Verdict;

import java.math.BigDecimal;
import java.util.List;

/**
 * Value Object: resultado completo de una simulación de préstamo.
 */
public record SimulationResult(
    BigDecimal monthlyPayment,
    BigDecimal tae,
    BigDecimal tinApplied,
    BigDecimal totalCost,
    BigDecimal totalInterest,
    BigDecimal effortRatio,         // cuota / ingreso neto
    BigDecimal debtRatio,           // (cuota + otras deudas) / ingreso neto
    BigDecimal ltvComputed,
    TaxInfo taxes,
    BigDecimal requiredOwnFunds,
    BigDecimal fundsGap,            // negativo si hay superávit
    int approvabilityScore,         // 0..100
    Verdict verdict,
    List<String> warnings,
    List<AlternativeProduct> alternatives
) {
    public record AlternativeProduct(
        String productId,
        String name,
        BigDecimal ltvMax,
        double fit
    ) {}

    public SimulationResult {
        warnings     = warnings     == null ? List.of() : List.copyOf(warnings);
        alternatives = alternatives == null ? List.of() : List.copyOf(alternatives);
    }
}
