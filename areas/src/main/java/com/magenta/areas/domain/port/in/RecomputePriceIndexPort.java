package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PropertyType;

import java.time.LocalDate;
import java.util.UUID;

public interface RecomputePriceIndexPort {

    record Command(UUID tenantId, UUID zoneId, PropertyType propertyType,
                   OperationType operationType, LocalDate period) {}

    void execute(Command command);
}
