package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.ContractType;
import com.magenta.banks.domain.model.Verdict;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;

import java.math.BigDecimal;

/**
 * Calcula el score de aprobabilidad (0..100) y el veredicto de una simulación.
 *
 * Composición lineal ponderada (suma 100):
 *   effortRatio    30 ptos
 *   debtRatio      20 ptos
 *   ownFunds gap   20 ptos
 *   contractType   10 ptos
 *   seniority       5 ptos
 *   age vs termEnd  5 ptos
 *   dependents      5 ptos
 *   CIRBE clean     5 ptos
 */
public class ApprovabilityScorerService {

    // Umbrales para veredicto
    private static final int APPROVABLE_THRESHOLD = 75;
    private static final int TIGHT_THRESHOLD      = 50;

    public record ScoreResult(int score, Verdict verdict) {}

    /**
     * @param effortRatio      cuota / ingreso neto (ej. 0.28)
     * @param debtRatio        (cuota + otras deudas) / ingreso neto
     * @param fundsGap         fondos faltantes en € (0 o negativo = OK)
     * @param propertyPrice    precio del inmueble (para relativizar el gap)
     * @param borrower         perfil del prestatario
     * @param termMonths       plazo del préstamo
     * @return score y veredicto
     */
    public ScoreResult score(BigDecimal effortRatio, BigDecimal debtRatio,
                             BigDecimal fundsGap, BigDecimal propertyPrice,
                             BorrowerProfile borrower, int termMonths) {

        int total = 0;
        total += scoreEffortRatio(effortRatio);
        total += scoreDebtRatio(debtRatio);
        total += scoreFundsGap(fundsGap, propertyPrice);
        total += scoreContractType(borrower.contractType());
        total += scoreSeniority(borrower.seniorityMonths());
        total += scoreAge(borrower.ageAtTermEnd(termMonths));
        total += scoreDependents(borrower.dependents());
        total += scoreCirbe(borrower.cirbeFlag());

        int capped = Math.max(0, Math.min(100, total));
        Verdict verdict = capped >= APPROVABLE_THRESHOLD ? Verdict.APPROVABLE
                        : capped >= TIGHT_THRESHOLD      ? Verdict.TIGHT
                                                         : Verdict.REJECTABLE;
        return new ScoreResult(capped, verdict);
    }

    // ── factores individuales (0..max_peso) ─────────────────────────────────

    /** effortRatio ≤ 0.30 → 30p; ≤ 0.35 → 20p; ≤ 0.40 → 10p; > 0.40 → 0 */
    private int scoreEffortRatio(BigDecimal r) {
        double v = r.doubleValue();
        if (v <= 0.30) return 30;
        if (v <= 0.35) return 20;
        if (v <= 0.40) return 10;
        return 0;
    }

    /** debtRatio ≤ 0.35 → 20p; ≤ 0.40 → 12p; ≤ 0.45 → 5p; > 0.45 → 0 */
    private int scoreDebtRatio(BigDecimal r) {
        double v = r.doubleValue();
        if (v <= 0.35) return 20;
        if (v <= 0.40) return 12;
        if (v <= 0.45) return  5;
        return 0;
    }

    /**
     * gap <= 0 (superávit) → 20p
     * gap ≤ 5% precio     → 12p
     * gap ≤ 10%           → 5p
     * > 10%               → 0
     */
    private int scoreFundsGap(BigDecimal gap, BigDecimal price) {
        if (gap == null || gap.signum() <= 0) return 20;
        if (price == null || price.signum() == 0) return 0;
        double gapPct = gap.doubleValue() / price.doubleValue();
        if (gapPct <= 0.05) return 12;
        if (gapPct <= 0.10) return  5;
        return 0;
    }

    private int scoreContractType(ContractType ct) {
        return switch (ct) {
            case CIVIL_SERVANT -> 10;
            case INDEFINITE    -> 10;
            case SELF_EMPLOYED ->  6;
            case TEMPORARY     ->  3;
            case PENSIONER     ->  8;
            default            ->  0;
        };
    }

    /** > 24 meses → 5p; 12-24 → 3p; < 12 → 0 */
    private int scoreSeniority(int months) {
        if (months >= 24) return 5;
        if (months >= 12) return 3;
        return 0;
    }

    /** edad al vencimiento ≤ 65 → 5p; ≤ 70 → 2p; > 70 → 0 */
    private int scoreAge(int ageAtEnd) {
        if (ageAtEnd <= 65) return 5;
        if (ageAtEnd <= 70) return 2;
        return 0;
    }

    /** 0 dependents → 5p; 1 → 3p; 2 → 1p; ≥ 3 → 0 */
    private int scoreDependents(int n) {
        if (n == 0) return 5;
        if (n == 1) return 3;
        if (n == 2) return 1;
        return 0;
    }

    /** Sin CIRBE → 5p */
    private int scoreCirbe(boolean cirbeFlag) {
        return cirbeFlag ? 0 : 5;
    }
}
