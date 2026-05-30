package com.magenta.areas.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PriceIndexResponse(
        UUID id,
        UUID zoneId,
        String propertyType,
        String operationType,
        String period,
        BigDecimal pricePerSqm,
        String currency,
        BigDecimal yoyDeltaPct,
        BigDecimal momDeltaPct,
        Integer sampleSize,
        BigDecimal confidence,
        String source
) {}
