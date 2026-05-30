package com.magenta.banks.domain.model.loanproduct;

import java.util.List;
import java.util.Map;

/**
 * Value Object: reglas de elegibilidad serializables en JSON.
 *
 * Estructura declarativa evaluada por RuleEngine:
 * { "all": [ { "field":"borrower.age", "op":"<=", "value":67, "atTermEnd":true }, ... ] }
 */
public record EligibilityRules(
    List<Rule> all
) {
    public record Rule(
        String field,
        String op,       // <=, >=, ==, !=, IN, NOT_IN
        Object value,
        boolean atTermEnd
    ) {}

    public EligibilityRules {
        all = all == null ? List.of() : List.copyOf(all);
    }

    /** Conveniencia: sin reglas = siempre elegible */
    public static EligibilityRules empty() {
        return new EligibilityRules(List.of());
    }
}
