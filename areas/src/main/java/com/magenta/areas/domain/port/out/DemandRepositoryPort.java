package com.magenta.areas.domain.port.out;

import com.magenta.areas.domain.model.ZoneDemandSnapshot;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DemandRepositoryPort {

    void save(ZoneDemandSnapshot snapshot);

    Optional<ZoneDemandSnapshot> findByZoneAndPeriod(UUID zoneId, LocalDate period);
}
