package com.magenta.products.domain.event;

import com.magenta.products.domain.model.EnergyLetter;
import com.magenta.products.domain.model.GeoPoint;
import com.magenta.products.domain.model.OperationType;
import com.magenta.products.domain.model.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record PropertyPublished(
        UUID propertyId,
        UUID tenantId,
        UUID zoneId,
        PropertyType type,
        OperationType operationType,
        BigDecimal price,
        BigDecimal builtSqm,
        Integer rooms,
        GeoPoint coordinates,
        Set<String> features,
        EnergyLetter energyLetter,
        Instant occurredAt) {
}
