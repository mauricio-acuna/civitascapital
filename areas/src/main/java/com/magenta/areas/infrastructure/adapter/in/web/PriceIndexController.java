package com.magenta.areas.infrastructure.adapter.in.web;

import com.magenta.areas.application.GetPriceIndexUseCase;
import com.magenta.areas.application.RecomputePriceIndexUseCase;
import com.magenta.areas.domain.model.OperationType;
import com.magenta.areas.domain.model.PriceIndex;
import com.magenta.areas.domain.model.PropertyType;
import com.magenta.areas.domain.port.in.RecomputePriceIndexPort;
import com.magenta.areas.infrastructure.adapter.in.web.dto.PriceIndexResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/price-indices")
@Tag(name = "Price Indices", description = "€/m² indices by zone")
public class PriceIndexController {

    private final GetPriceIndexUseCase getPriceIndex;
    private final RecomputePriceIndexPort recompute;

    public PriceIndexController(GetPriceIndexUseCase getPriceIndex,
                                 RecomputePriceIndexPort recompute) {
        this.getPriceIndex = getPriceIndex;
        this.recompute     = recompute;
    }

    @GetMapping
    public List<PriceIndexResponse> series(
            @RequestParam UUID zoneId,
            @RequestParam(defaultValue = "FLAT") String type,
            @RequestParam(defaultValue = "SALE") String op,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate start = from != null ? from : LocalDate.now().minusYears(2);
        LocalDate end   = to   != null ? to   : LocalDate.now();
        return getPriceIndex.series(zoneId, PropertyType.valueOf(type), OperationType.valueOf(op), start, end)
                .stream().map(this::toDto).toList();
    }

    @GetMapping("/latest")
    public ResponseEntity<PriceIndexResponse> latest(
            @RequestParam UUID zoneId,
            @RequestParam(defaultValue = "FLAT") String type,
            @RequestParam(defaultValue = "SALE") String op) {
        return getPriceIndex.latest(zoneId, PropertyType.valueOf(type), OperationType.valueOf(op))
                .map(p -> ResponseEntity.ok(toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/recompute")
    @PreAuthorize("hasAnyRole('ADMIN','SYSTEM')")
    public void recompute(@RequestParam UUID zoneId,
                          @RequestParam String type,
                          @RequestParam String op,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period,
                          @RequestParam UUID tenantId) {
        recompute.execute(new RecomputePriceIndexPort.Command(
                tenantId, zoneId, PropertyType.valueOf(type), OperationType.valueOf(op), period));
    }

    private PriceIndexResponse toDto(PriceIndex p) {
        return new PriceIndexResponse(p.getId(), p.getZoneId(),
                p.getPropertyType().name(), p.getOperationType().name(),
                p.getPeriod().toString(),
                p.getPricePerSqm().amount(), p.getPricePerSqm().currency(),
                p.getYoyDeltaPct(), p.getMomDeltaPct(),
                p.getSampleSize(), p.getConfidence(), p.getSource().name());
    }
}
