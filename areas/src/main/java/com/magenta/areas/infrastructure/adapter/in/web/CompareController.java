package com.magenta.areas.infrastructure.adapter.in.web;

import com.magenta.areas.application.GetPriceIndexUseCase;
import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PropertyType;
import com.magenta.areas.domain.port.in.CompareZonesPort;
import com.magenta.areas.infrastructure.adapter.in.web.dto.ZoneCompareResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compare")
@Tag(name = "Compare", description = "Multi-zone comparison (UC-A6)")
public class CompareController {

    private final CompareZonesPort compareZones;

    public CompareController(CompareZonesPort compareZones) {
        this.compareZones = compareZones;
    }

    @GetMapping
    public ZoneCompareResponse compare(@RequestParam List<UUID> zoneIds) {
        List<CompareZonesPort.ZoneComparison> comparisons = compareZones.execute(zoneIds);

        List<ZoneCompareResponse.ZoneCompareItemDto> items = comparisons.stream()
                .map(c -> new ZoneCompareResponse.ZoneCompareItemDto(
                        c.zone().getId(),
                        c.zone().getName(),
                        c.zone().getType().name(),
                        c.latestSale()  != null ? c.latestSale().getPricePerSqm().amount()  : null,
                        c.latestRent()  != null ? c.latestRent().getPricePerSqm().amount()  : null,
                        c.enrichment()  != null ? c.enrichment().getHospitalKind().name()   : null,
                        c.enrichment()  != null ? c.enrichment().getFiberCoveragePct()       : null,
                        c.enrichment()  != null ? c.enrichment().getTrainToHubMinutes()      : null,
                        c.enrichment()  != null ? c.enrichment().getQualityOfLifeIndex()     : null))
                .toList();

        return new ZoneCompareResponse(items);
    }
}
