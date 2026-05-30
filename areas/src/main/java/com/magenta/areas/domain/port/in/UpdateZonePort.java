package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.Zone;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public interface UpdateZonePort {

    record Command(UUID id, String name, Set<String> postalCodes, Set<String> tags,
                   Integer population, BigDecimal areaKm2, String actorId) {}

    Zone execute(Command command);
}
