package com.magenta.areas.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ZoneCompareResponse(List<ZoneCompareItemDto> zones) {

    public record ZoneCompareItemDto(
            UUID id,
            String name,
            String type,
            BigDecimal salePricePerSqm,
            BigDecimal rentPricePerSqm,
            String hospitalKind,
            Integer fiberCoveragePct,
            Integer trainToHubMinutes,
            Integer qualityOfLifeIndex
    ) {}
}
