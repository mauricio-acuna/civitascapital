package com.magenta.areas.infrastructure.adapter.in.web;

import com.magenta.areas.application.*;
import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.in.*;
import com.magenta.areas.infrastructure.adapter.in.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/zones")
@Tag(name = "Zones", description = "Geographic zone management")
public class ZoneController {

    private final CreateZonePort createZone;
    private final UpdateZonePort updateZone;
    private final DeleteZonePort deleteZone;
    private final GetZonePort getZone;
    private final SearchZonesPort searchZones;
    private final ResolvePointPort resolvePoint;
    private final ImportGeoJsonPort importGeoJson;
    private final GetPriceIndexUseCase priceIndex;
    private final GetEnrichmentPort getEnrichment;

    public ZoneController(CreateZonePort createZone, UpdateZonePort updateZone,
                          DeleteZonePort deleteZone, GetZonePort getZone,
                          SearchZonesPort searchZones, ResolvePointPort resolvePoint,
                          ImportGeoJsonPort importGeoJson, GetPriceIndexUseCase priceIndex,
                          GetEnrichmentPort getEnrichment) {
        this.createZone   = createZone;
        this.updateZone   = updateZone;
        this.deleteZone   = deleteZone;
        this.getZone      = getZone;
        this.searchZones  = searchZones;
        this.resolvePoint = resolvePoint;
        this.importGeoJson = importGeoJson;
        this.priceIndex   = priceIndex;
        this.getEnrichment = getEnrichment;
    }

    // ── GET /zones/search ────────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Autocomplete zones by text (UC-A1)")
    public List<ZoneDetailResponse> search(
            @RequestParam String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "10") int limit) {

        List<ZoneType> zoneTypes = types != null
                ? types.stream().map(ZoneType::valueOf).toList() : List.of();
        return searchZones.execute(new SearchZonesPort.Query(q, zoneTypes, limit))
                .stream().map(z -> toDetailResponse(z, null, null, null)).toList();
    }

    // ── GET /zones/resolve ───────────────────────────────────────────────────

    @GetMapping("/resolve")
    @Operation(summary = "Resolve lat/lng to zone (UC-A3)")
    public ResponseEntity<ZoneDetailResponse> resolve(
            @RequestParam double lat, @RequestParam double lng) {
        return resolvePoint.execute(lat, lng)
                .map(z -> ResponseEntity.ok(toDetailResponse(z, null, null, null)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /zones/by-postal-code/{cp} ───────────────────────────────────────

    @GetMapping("/by-postal-code/{cp}")
    public List<ZoneDetailResponse> byPostalCode(@PathVariable String cp) {
        return getZone.byPostalCode(cp).stream()
                .map(z -> toDetailResponse(z, null, null, null)).toList();
    }

    // ── GET /zones ────────────────────────────────────────────────────────────

    @GetMapping
    public List<ZoneDetailResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "20") int limit) {
        if (q != null && !q.isBlank()) return search(q, types, limit);
        return List.of(); // sin filtro devuelve vacío para evitar full-scan
    }

    // ── GET /zones/{id} ───────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Zone detail with enrichment and latest prices (UC-A2)")
    public ResponseEntity<ZoneDetailResponse> getById(@PathVariable UUID id) {
        return getZone.byId(id).map(zone -> {
            ZoneEnrichment enrichment = getEnrichment.execute(id).orElse(null);
            PriceIndex sale = priceIndex.latest(id, PropertyType.FLAT, OperationType.SALE).orElse(null);
            PriceIndex rent = priceIndex.latest(id, PropertyType.FLAT, OperationType.RENT).orElse(null);
            return ResponseEntity.ok(toDetailResponse(zone, enrichment, sale, rent));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── GET /zones/{id}/children ──────────────────────────────────────────────

    @GetMapping("/{id}/children")
    public List<ZoneDetailResponse> children(@PathVariable UUID id) {
        return getZone.children(id).stream()
                .map(z -> toDetailResponse(z, null, null, null)).toList();
    }

    // ── GET /zones/{id}/ancestors ─────────────────────────────────────────────

    @GetMapping("/{id}/ancestors")
    public List<ZoneDetailResponse> ancestors(@PathVariable UUID id) {
        return getZone.ancestors(id).stream()
                .map(z -> toDetailResponse(z, null, null, null)).toList();
    }

    // ── POST /zones ───────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ZoneDetailResponse create(@Valid @RequestBody ZoneRequest req,
                                      @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        String actor  = jwt.getSubject();
        Zone zone = createZone.execute(new CreateZonePort.Command(
                tenantId, req.code(), req.name(), ZoneType.valueOf(req.type()),
                req.parentId(), new GeoPoint(req.lat(), req.lng()), actor));
        return toDetailResponse(zone, null, null, null);
    }

    // ── PUT /zones/{id} ───────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ZoneDetailResponse update(@PathVariable UUID id,
                                      @Valid @RequestBody ZoneRequest req,
                                      @AuthenticationPrincipal Jwt jwt) {
        String actor = jwt.getSubject();
        Zone zone = updateZone.execute(new UpdateZonePort.Command(
                id, req.name(),
                req.postalCodes() != null ? req.postalCodes() : Set.of(),
                req.tags() != null ? req.tags() : Set.of(),
                req.population(), req.areaKm2(), actor));
        return toDetailResponse(zone, null, null, null);
    }

    // ── DELETE /zones/{id} ────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        deleteZone.execute(id, jwt.getSubject());
    }

    // ── POST /zones/import ────────────────────────────────────────────────────

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> importGeoJson(@RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) String sha256,
                                              @AuthenticationPrincipal Jwt jwt) throws IOException {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        int count = importGeoJson.execute(new ImportGeoJsonPort.Command(
                tenantId, file.getInputStream(), file.getOriginalFilename(),
                file.getSize(), sha256, jwt.getSubject()));
        return Map.of("imported", count);
    }

    // ── mapping helper ────────────────────────────────────────────────────────

    private ZoneDetailResponse toDetailResponse(Zone zone, ZoneEnrichment enrichment,
                                                  PriceIndex sale, PriceIndex rent) {
        ZoneDetailResponse.EnrichmentDto enrichDto = enrichment == null ? null
                : new ZoneDetailResponse.EnrichmentDto(
                    enrichment.getFiberCoveragePct(), enrichment.isHasHospital(),
                    enrichment.getHospitalKind().name(), enrichment.getTrainToHubMinutes(),
                    enrichment.getHighwayDistanceKm(), enrichment.getSupermarketsCount(),
                    enrichment.getRiskOccupationScore(), enrichment.getDepopulationRisk().name(),
                    enrichment.getQualityOfLifeIndex());

        ZoneDetailResponse.PriceDto saleDto = sale == null ? null
                : new ZoneDetailResponse.PriceDto(sale.getPricePerSqm().amount(),
                    sale.getYoyDeltaPct(), sale.getPeriod().toString());
        ZoneDetailResponse.PriceDto rentDto = rent == null ? null
                : new ZoneDetailResponse.PriceDto(rent.getPricePerSqm().amount(),
                    rent.getYoyDeltaPct(), rent.getPeriod().toString());

        return new ZoneDetailResponse(
                zone.getId(), zone.getCode(), zone.getName(), zone.getType().name(),
                null, // parent — cargado aparte si se necesita
                zone.getIneCode(), zone.getPostalCodes(),
                new ZoneDetailResponse.GeoPointDto(zone.getCentroid().lat(), zone.getCentroid().lng()),
                zone.getPopulation(), zone.getAreaKm2(), zone.getStatus().name(),
                zone.getTags(), enrichDto,
                (saleDto != null || rentDto != null) ? new ZoneDetailResponse.LatestPriceDto(saleDto, rentDto) : null,
                zone.getCreatedAt(), zone.getUpdatedAt());
    }
}
