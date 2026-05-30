package com.magenta.areas.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ZoneDetailResponse(
        UUID id,
        String code,
        String name,
        String type,
        ZoneRefDto parent,
        String ineCode,
        Set<String> postalCodes,
        GeoPointDto centroid,
        Integer population,
        BigDecimal areaKm2,
        String status,
        Set<String> tags,
        EnrichmentDto enrichment,
        LatestPriceDto latestPrice,
        Instant createdAt,
        Instant updatedAt
) {

    public record ZoneRefDto(UUID id, String name, String type) {}

    public record GeoPointDto(double lat, double lng) {}

    public record EnrichmentDto(
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

    public record PriceDto(BigDecimal pricePerSqm, BigDecimal yoyDeltaPct, String period) {}

    public record LatestPriceDto(PriceDto sale, PriceDto rent) {}
}
