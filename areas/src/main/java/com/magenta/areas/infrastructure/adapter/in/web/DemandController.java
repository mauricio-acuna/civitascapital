package com.magenta.areas.infrastructure.adapter.in.web;

import com.magenta.areas.domain.model.ZoneDemandSnapshot;
import com.magenta.areas.domain.port.out.DemandRepositoryPort;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demand")
@Tag(name = "Demand", description = "Zone demand snapshots")
public class DemandController {

    private final DemandRepositoryPort demandRepo;

    public DemandController(DemandRepositoryPort demandRepo) {
        this.demandRepo = demandRepo;
    }

    @GetMapping("/{zoneId}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ResponseEntity<Map<String, Object>> getDemand(
            @PathVariable UUID zoneId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") String period) {

        LocalDate date = period != null
                ? LocalDate.parse(period + "-01") : LocalDate.now().withDayOfMonth(1);

        return demandRepo.findByZoneAndPeriod(zoneId, date)
                .map(s -> ResponseEntity.ok(toMap(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(ZoneDemandSnapshot s) {
        return Map.of(
                "zoneId", s.getZoneId(),
                "period", s.getPeriod().toString(),
                "searches", s.getSearches(),
                "leads", s.getLeads(),
                "viewedProperties", s.getViewedProperties(),
                "savedSearches", s.getSavedSearches(),
                "supplyDemandRatio", s.getSupplyDemandRatio() != null ? s.getSupplyDemandRatio() : BigDecimal.ZERO);
    }
}
