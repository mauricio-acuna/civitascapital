package com.magenta.banks.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EuriborRate(
        LocalDate period,
        BigDecimal rate12mPct,
        String source
) {}
