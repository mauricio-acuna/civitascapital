package com.magenta.products.domain.model;

import java.math.BigDecimal;

public record Surface(BigDecimal builtSqm, BigDecimal usefulSqm, BigDecimal plotSqm) {

    public Surface {
        if (builtSqm == null || builtSqm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("builtSqm must be > 0");
        }
    }

    public static Surface of(BigDecimal builtSqm, BigDecimal usefulSqm, BigDecimal plotSqm) {
        return new Surface(builtSqm, usefulSqm, plotSqm);
    }
}
