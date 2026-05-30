package com.magenta.banks.domain.model.loansimulation;

import com.magenta.banks.domain.model.ContractType;

import java.math.BigDecimal;

/**
 * Contexto de evaluación pasado al RuleEngine.
 * Resuelve nombres de campo declarativos a valores reales del prestatario/simulación.
 */
public record EvaluationContext(
    BorrowerProfile borrower,
    int termMonths,
    BigDecimal effortRatio,
    BigDecimal debtRatio,
    BigDecimal ltv
) {

    /**
     * Resuelve un campo por nombre.
     *
     * @param field     nombre del campo, ej. "borrower.age", "result.effortRatio"
     * @param atTermEnd si true, usa la edad al vencimiento en lugar de la actual
     */
    public Object resolve(String field, boolean atTermEnd) {
        return switch (field) {
            case "borrower.age"          -> atTermEnd ? borrower.ageAtTermEnd(termMonths) : borrower.age();
            case "borrower.contractType" -> borrower.contractType().name();
            case "borrower.cirbeFlag"    -> borrower.cirbeFlag();
            case "result.effortRatio"    -> effortRatio;
            case "result.debtRatio"      -> debtRatio;
            case "ltv"                   -> ltv;
            default                      -> null;
        };
    }
}
