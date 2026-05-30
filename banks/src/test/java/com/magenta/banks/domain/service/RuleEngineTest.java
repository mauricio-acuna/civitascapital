package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.ContractType;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import com.magenta.banks.domain.model.loansimulation.EvaluationContext;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    private RuleEngine engine;

    @BeforeEach
    void setUp() { engine = new RuleEngine(); }

    private BorrowerProfile borrower(int age, ContractType ct, boolean cirbe) {
        return new BorrowerProfile(
                BigDecimal.valueOf(2500), 12, age, ct,
                36, BigDecimal.ZERO, 0, BigDecimal.ZERO, false, cirbe);
    }

    private EvaluationContext ctx(BorrowerProfile b, int termMonths,
                                   double effort, double debt, double ltv) {
        return new EvaluationContext(b, termMonths,
                BigDecimal.valueOf(effort), BigDecimal.valueOf(debt), BigDecimal.valueOf(ltv));
    }

    @Test
    @DisplayName("Sin reglas → siempre elegible")
    void emptyRulesAlwaysEligible() {
        EligibilityRules rules = EligibilityRules.empty();
        assertThat(engine.evaluate(rules, ctx(borrower(40, ContractType.INDEFINITE, false), 360, 0.30, 0.30, 0.80)))
                .isTrue();
    }

    @Test
    @DisplayName("Regla edad al vencimiento: 40 + 360m = 70 → OK (≤70)")
    void ageAtTermEndOk() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("borrower.age", "<=", 67, true)
        ));
        BorrowerProfile b = borrower(37, ContractType.INDEFINITE, false); // 37 + 30 = 67 ✓
        assertThat(engine.evaluate(rules, ctx(b, 360, 0.30, 0.30, 0.80))).isTrue();
    }

    @Test
    @DisplayName("Regla edad al vencimiento: 45 + 360m = 75 → rechazado")
    void ageAtTermEndFail() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("borrower.age", "<=", 67, true)
        ));
        BorrowerProfile b = borrower(45, ContractType.INDEFINITE, false); // 45 + 30 = 75 ✗
        assertThat(engine.evaluate(rules, ctx(b, 360, 0.30, 0.30, 0.80))).isFalse();
    }

    @Test
    @DisplayName("Regla contractType IN [INDEFINITE, CIVIL_SERVANT]")
    void contractTypeIn() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("borrower.contractType", "IN", List.of("INDEFINITE", "CIVIL_SERVANT"), false)
        ));
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, false), 240, 0.28, 0.28, 0.80)))
                .isTrue();
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.TEMPORARY, false), 240, 0.28, 0.28, 0.80)))
                .isFalse();
    }

    @Test
    @DisplayName("Regla effortRatio ≤ 0.35")
    void effortRatioLimit() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("result.effortRatio", "<=", 0.35, false)
        ));
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, false), 240, 0.34, 0.34, 0.80)))
                .isTrue();
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, false), 240, 0.36, 0.36, 0.80)))
                .isFalse();
    }

    @Test
    @DisplayName("Regla cirbeFlag == false")
    void cirbeFlagMustBeFalse() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("borrower.cirbeFlag", "==", false, false)
        ));
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, false), 240, 0.28, 0.28, 0.80)))
                .isTrue();
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, true), 240, 0.28, 0.28, 0.80)))
                .isFalse();
    }

    @Test
    @DisplayName("Regla LTV ≤ 0.90")
    void ltvLimit() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("ltv", "<=", 0.90, false)
        ));
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, false), 240, 0.28, 0.28, 0.90)))
                .isTrue();
        assertThat(engine.evaluate(rules, ctx(borrower(35, ContractType.INDEFINITE, false), 240, 0.28, 0.28, 0.91)))
                .isFalse();
    }

    @Test
    @DisplayName("Reglas completas del spec: todas satisfechas")
    void fullSpecRulesPass() {
        EligibilityRules rules = new EligibilityRules(List.of(
                new Rule("borrower.age",          "<=", 67,   true),
                new Rule("borrower.contractType", "IN", List.of("INDEFINITE","CIVIL_SERVANT"), false),
                new Rule("result.effortRatio",    "<=", 0.35, false),
                new Rule("result.debtRatio",      "<=", 0.40, false),
                new Rule("ltv",                   "<=", 0.90, false),
                new Rule("borrower.cirbeFlag",    "==", false, false)
        ));
        BorrowerProfile b = borrower(35, ContractType.INDEFINITE, false); // 35+30=65 ✓
        assertThat(engine.evaluate(rules, ctx(b, 360, 0.28, 0.28, 0.85))).isTrue();
    }
}
