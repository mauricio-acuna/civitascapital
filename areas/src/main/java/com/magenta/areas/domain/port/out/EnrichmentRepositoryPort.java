package com.magenta.areas.domain.port.out;

import com.magenta.areas.domain.model.ZoneEnrichment;

import java.util.Optional;
import java.util.UUID;

public interface EnrichmentRepositoryPort {

    void save(ZoneEnrichment enrichment);

    Optional<ZoneEnrichment> findByZoneId(UUID zoneId);
}
