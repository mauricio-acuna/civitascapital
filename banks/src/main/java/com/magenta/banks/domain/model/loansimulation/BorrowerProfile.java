package com.magenta.banks.domain.model.loansimulation;

import com.magenta.banks.domain.model.ContractType;

import java.math.BigDecimal;

/**
 * Value Object: perfil económico del prestatario para una simulación.
 */
public record BorrowerProfile(
    BigDecimal netIncomeMonthly,
    int payments,               // nóminas al año (12 o 14)
    int age,
    ContractType contractType,
    int seniorityMonths,
    BigDecimal otherDebtMonthly,
    int dependents,
    BigDecimal ownFunds,
    boolean hasGuarantor,
    boolean cirbeFlag           // true = aparece en CIRBE con incidencias
) {
    public BorrowerProfile {
        if (netIncomeMonthly == null || netIncomeMonthly.signum() <= 0)
            throw new IllegalArgumentException("netIncomeMonthly must be positive");
        if (age < 18)
            throw new IllegalArgumentException("borrower age must be >= 18");
    }

    public int ageAtTermEnd(int termMonths) {
        return age + (termMonths / 12);
    }
}
