package com.magenta.banks.domain.model.loanproduct;

import com.magenta.banks.domain.model.RateType;

import java.math.BigDecimal;

/**
 * Value Object: información de tipo de interés del producto.
 */
public record RateInfo(
    RateType rateType,
    BigDecimal initialPct,
    String indexReference,   // ej. EURIBOR_12M — null si FIXED
    BigDecimal marginPct,    // null si FIXED
    Integer fixedYears       // null si no MIXED
) {
    public RateInfo {
        if (rateType == null) throw new IllegalArgumentException("rateType is required");
        if (initialPct == null) throw new IllegalArgumentException("initialPct is required");
    }

    public boolean isVariable() {
        return rateType == RateType.VARIABLE_EURIBOR || rateType == RateType.MIXED;
    }

    /** TIN estresado = indexReference + margin + 100 bps */
    public BigDecimal stressedMarginPct() {
        if (marginPct == null) return null;
        return marginPct.add(BigDecimal.valueOf(1.0));
    }
}
