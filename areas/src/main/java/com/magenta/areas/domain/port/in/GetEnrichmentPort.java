package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.ZoneEnrichment;

import java.util.Optional;
import java.util.UUID;

public interface GetEnrichmentPort {

    Optional<ZoneEnrichment> execute(UUID zoneId);
}
