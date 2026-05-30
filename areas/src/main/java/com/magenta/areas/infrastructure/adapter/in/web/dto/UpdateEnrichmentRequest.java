package com.magenta.areas.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateEnrichmentRequest(
        Integer fiberCoveragePct,
        Boolean hasHospital,
        String hospitalKind,
        Integer trainToHubMinutes,
        BigDecimal highwayDistanceKm,
        Integer supermarketsCount,
        Integer riskOccupationScore,
        String depopulationRisk,
        Integer qualityOfLifeIndex
) {}
