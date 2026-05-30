package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import com.magenta.banks.domain.model.loansimulation.EvaluationContext;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules.Rule;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Motor de reglas puro Java: evalúa {@link EligibilityRules} contra un contexto de evaluación.
 * Sin dependencias de Spring ni frameworks externos. Completamente testeable unitariamente.
 */
public class RuleEngine {

    /**
     * @return true si el borrower supera TODAS las reglas del producto.
     */
    public boolean evaluate(EligibilityRules rules, EvaluationContext ctx) {
        for (Rule rule : rules.all()) {
            if (!evaluateRule(rule, ctx)) return false;
        }
        return true;
    }

    private boolean evaluateRule(Rule rule, EvaluationContext ctx) {
        Object actualValue = ctx.resolve(rule.field(), rule.atTermEnd());
        if (actualValue == null) return false;

        return switch (rule.op()) {
            case "<="    -> compareNumbers(actualValue, rule.value()) <= 0;
            case ">="    -> compareNumbers(actualValue, rule.value()) >= 0;
            case "<"     -> compareNumbers(actualValue, rule.value()) <  0;
            case ">"     -> compareNumbers(actualValue, rule.value()) >  0;
            case "=="    -> equalsValue(actualValue, rule.value());
            case "!="    -> !equalsValue(actualValue, rule.value());
            case "IN"    -> containsValue(rule.value(), actualValue);
            case "NOT_IN"-> !containsValue(rule.value(), actualValue);
            default      -> throw new IllegalArgumentException("Unknown operator: " + rule.op());
        };
    }

    private int compareNumbers(Object actual, Object expected) {
        BigDecimal a = toBigDecimal(actual);
        BigDecimal e = toBigDecimal(expected);
        return a.compareTo(e);
    }

    private boolean equalsValue(Object actual, Object expected) {
        if (actual instanceof Boolean b && expected instanceof Boolean e) return b.equals(e);
        if (actual instanceof String  s && expected instanceof String  e) return s.equals(e);
        try { return compareNumbers(actual, expected) == 0; }
        catch (NumberFormatException ex) { return actual.equals(expected); }
    }

    @SuppressWarnings("unchecked")
    private boolean containsValue(Object listValue, Object target) {
        if (listValue instanceof Collection<?> col) {
            return col.stream().anyMatch(item -> equalsValue(target, item));
        }
        if (listValue instanceof List<?> list) {
            return list.stream().anyMatch(item -> equalsValue(target, item));
        }
        return false;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n)     return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }
}
