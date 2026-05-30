package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.DepopulationRisk;
import com.magenta.areas.domain.model.HospitalKind;
import com.magenta.areas.domain.model.ZoneEnrichment;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateEnrichmentPort {

    record Command(UUID zoneId, Integer fiberCoveragePct, boolean hasHospital,
                   HospitalKind hospitalKind, Integer trainToHubMinutes,
                   BigDecimal highwayDistanceKm, Integer supermarketsCount,
                   Integer riskOccupationScore, DepopulationRisk depopulationRisk,
                   Integer qualityOfLifeIndex, String actorId) {}

    ZoneEnrichment execute(Command command);
}
