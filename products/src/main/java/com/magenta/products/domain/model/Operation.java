package com.magenta.products.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Operation(
        UUID id,
        UUID propertyId,
        OperationType type,
        Money price,
        Integer depositMonths,
        Integer minContractMonths,
        RentToOwnTerms rentToOwn,
        ExchangeWishes exchangeWishes,
        boolean negotiable,
        LocalDate availableFrom,
        BigDecimal commissionPct,
        OperationStatus status,
        boolean exclusivity,
        java.time.Instant publishedAt) {

    public record RentToOwnTerms(BigDecimal optionFeePct, int optionExerciseMonths, Money salePrice) {}

    public record ExchangeWishes(List<PropertyType> types, List<UUID> zoneIds, Money maxDelta) {}
}
