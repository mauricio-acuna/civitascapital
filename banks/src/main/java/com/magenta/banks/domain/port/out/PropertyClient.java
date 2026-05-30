package com.magenta.banks.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto outbound hacia el módulo products.
 */
public interface PropertyClient {

    record PropertyInfo(
        UUID propertyId,
        BigDecimal price,
        BigDecimal surfaceSqm,
        String type,          // FLAT, HOUSE, LAND…
        String operationType, // SALE, RENT
        UUID zoneId
    ) {}

    Optional<PropertyInfo> getPropertyInfo(UUID propertyId);
}
