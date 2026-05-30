package com.magenta.servicios.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductClientPort {
    PropertyData getProperty(UUID propertyId);

    record PropertyData(UUID id, UUID zoneId, String operationType,
                        BigDecimal price, String type) {}
}
