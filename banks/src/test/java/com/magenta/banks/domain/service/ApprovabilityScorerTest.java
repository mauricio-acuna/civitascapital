package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.ContractType;
import com.magenta.banks.domain.model.Verdict;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovabilityScorerTest {

    private ApprovabilityScorerService scorer;

    @BeforeEach
    void setUp() { scorer = new ApprovabilityScorerService(); }

    private BorrowerProfile profile(int age, ContractType ct, int seniorityMonths,
                                     int dependents, boolean cirbe) {
        return new BorrowerProfile(BigDecimal.valueOf(3000), 12, age, ct,
                seniorityMonths, BigDecimal.ZERO, dependents, BigDecimal.valueOf(30_000), false, cirbe);
    }

    @Test
    @DisplayName("Perfil ideal: APPROVABLE con score ≥ 75")
    void idealProfileIsApprovable() {
        BorrowerProfile b = profile(30, ContractType.CIVIL_SERVANT, 60, 0, false);
        var result = scorer.score(
                BigDecimal.valueOf(0.25), BigDecimal.valueOf(0.25),
                BigDecimal.valueOf(-5000),  // superávit
                BigDecimal.valueOf(200_000), b, 360);
        assertThat(result.verdict()).isEqualTo(Verdict.APPROVABLE);
        assertThat(result.score()).isGreaterThanOrEqualTo(75);
    }

    @Test
    @DisplayName("Perfil límite: TIGHT entre 50 y 74")
    void tightProfile() {
        BorrowerProfile b = profile(40, ContractType.SELF_EMPLOYED, 18, 2, false);
        var result = scorer.score(
                BigDecimal.valueOf(0.33), BigDecimal.valueOf(0.38),
                BigDecimal.valueOf(3000),   // pequeño gap
                BigDecimal.valueOf(180_000), b, 300);
        assertThat(result.verdict()).isIn(Verdict.TIGHT, Verdict.APPROVABLE);
    }

    @Test
    @DisplayName("Perfil malo: REJECTABLE con score < 50")
    void rejectableProfile() {
        BorrowerProfile b = profile(55, ContractType.UNEMPLOYED, 0, 3, true);
        var result = scorer.score(
                BigDecimal.valueOf(0.50), BigDecimal.valueOf(0.55),
                BigDecimal.valueOf(25_000),  // gran gap
                BigDecimal.valueOf(150_000), b, 360);
        assertThat(result.verdict()).isEqualTo(Verdict.REJECTABLE);
        assertThat(result.score()).isLessThan(50);
    }

    @Test
    @DisplayName("CIRBE penaliza: reduce el score en 5 puntos")
    void cirbePenalty() {
        BorrowerProfile noCirbe = profile(30, ContractType.INDEFINITE, 36, 0, false);
        BorrowerProfile withCirbe = profile(30, ContractType.INDEFINITE, 36, 0, true);

        var r1 = scorer.score(BigDecimal.valueOf(0.25), BigDecimal.valueOf(0.25),
                BigDecimal.ZERO, BigDecimal.valueOf(200_000), noCirbe, 360);
        var r2 = scorer.score(BigDecimal.valueOf(0.25), BigDecimal.valueOf(0.25),
                BigDecimal.ZERO, BigDecimal.valueOf(200_000), withCirbe, 360);

        assertThat(r1.score() - r2.score()).isEqualTo(5);
    }
}
