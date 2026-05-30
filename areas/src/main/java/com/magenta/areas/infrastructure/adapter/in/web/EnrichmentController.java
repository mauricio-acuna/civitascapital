package com.magenta.areas.infrastructure.adapter.in.web;

import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.port.in.GetEnrichmentPort;
import com.magenta.areas.domain.port.in.UpdateEnrichmentPort;
import com.magenta.areas.infrastructure.adapter.in.web.dto.UpdateEnrichmentRequest;
import com.magenta.areas.infrastructure.adapter.in.web.dto.ZoneDetailResponse.EnrichmentDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.magenta.areas.domain.model.DepopulationRisk.LOW;
import static com.magenta.areas.domain.model.HospitalKind.NONE;

@RestController
@RequestMapping("/api/v1/enrichment")
@Tag(name = "Enrichment", description = "Zone enrichment data")
public class EnrichmentController {

    private final GetEnrichmentPort getEnrichment;
    private final UpdateEnrichmentPort updateEnrichment;

    public EnrichmentController(GetEnrichmentPort getEnrichment,
                                 UpdateEnrichmentPort updateEnrichment) {
        this.getEnrichment    = getEnrichment;
        this.updateEnrichment = updateEnrichment;
    }

    @GetMapping("/{zoneId}")
    public ResponseEntity<EnrichmentDto> get(@PathVariable UUID zoneId) {
        return getEnrichment.execute(zoneId)
                .map(e -> ResponseEntity.ok(toDto(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{zoneId}")
    @PreAuthorize("hasRole('ADMIN')")
    public EnrichmentDto update(@PathVariable UUID zoneId,
                                 @RequestBody UpdateEnrichmentRequest req,
                                 @AuthenticationPrincipal Jwt jwt) {
        var hospitalKind     = req.hospitalKind() != null
                ? com.magenta.areas.domain.model.HospitalKind.valueOf(req.hospitalKind()) : NONE;
        var depopulationRisk = req.depopulationRisk() != null
                ? com.magenta.areas.domain.model.DepopulationRisk.valueOf(req.depopulationRisk()) : LOW;

        ZoneEnrichment result = updateEnrichment.execute(new UpdateEnrichmentPort.Command(
                zoneId, req.fiberCoveragePct(),
                req.hasHospital() != null && req.hasHospital(),
                hospitalKind, req.trainToHubMinutes(), req.highwayDistanceKm(),
                req.supermarketsCount(), req.riskOccupationScore(),
                depopulationRisk, req.qualityOfLifeIndex(), jwt.getSubject()));
        return toDto(result);
    }

    private EnrichmentDto toDto(ZoneEnrichment e) {
        return new EnrichmentDto(
                e.getFiberCoveragePct(), e.isHasHospital(),
                e.getHospitalKind().name(), e.getTrainToHubMinutes(),
                e.getHighwayDistanceKm(), e.getSupermarketsCount(),
                e.getRiskOccupationScore(), e.getDepopulationRisk().name(),
                e.getQualityOfLifeIndex());
    }
}
