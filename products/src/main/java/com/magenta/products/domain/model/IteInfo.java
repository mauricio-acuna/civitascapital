package com.magenta.products.domain.model;

import java.time.LocalDate;

public record IteInfo(
        boolean hasIte,
        String result,      // FAVORABLE | UNFAVORABLE
        LocalDate issuedAt,
        LocalDate nextDueAt) {
}
