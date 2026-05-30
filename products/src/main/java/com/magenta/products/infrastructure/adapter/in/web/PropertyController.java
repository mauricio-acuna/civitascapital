package com.magenta.products.infrastructure.adapter.in.web;

import com.magenta.products.application.*;
import com.magenta.products.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Properties", description = "Property catalogue management")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final PublishPropertyUseCase publishPropertyUseCase;
    private final ArchivePropertyUseCase archivePropertyUseCase;
    private final SearchPropertiesUseCase searchPropertiesUseCase;
    private final GetPropertyUseCase getPropertyUseCase;

    public PropertyController(CreatePropertyUseCase createPropertyUseCase,
                               PublishPropertyUseCase publishPropertyUseCase,
                               ArchivePropertyUseCase archivePropertyUseCase,
                               SearchPropertiesUseCase searchPropertiesUseCase,
                               GetPropertyUseCase getPropertyUseCase) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.publishPropertyUseCase = publishPropertyUseCase;
        this.archivePropertyUseCase = archivePropertyUseCase;
        this.searchPropertiesUseCase = searchPropertiesUseCase;
        this.getPropertyUseCase = getPropertyUseCase;
    }

    @GetMapping
    @Operation(summary = "List properties (basic filter)")
    public ResponseEntity<List<PropertyDetailResponse>> listProperties(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @AuthenticationPrincipal Jwt jwt) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        int fetchLimit = Math.min(100, safeSize * (safePage + 1));

        List<PropertyDetailResponse> results = searchPropertiesUseCase.execute(
                        new SearchPropertiesUseCase.Query(
                                tenantId(jwt, tenantHeader),
                                parseEnum(PropertyStatus.class, status),
                                parseEnum(PropertyType.class, type),
                                zoneId,
                                parseEnum(OperationType.class, operationType),
                                minPrice,
                                maxPrice,
                                fetchLimit))
                .stream()
                .skip((long) safePage * safeSize)
                .limit(safeSize)
                .map(PropertyDetailResponse::from)
                .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property detail")
    public ResponseEntity<PropertyDetailResponse> getProperty(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @AuthenticationPrincipal Jwt jwt) {
        Property property = getPropertyUseCase.execute(tenantId(jwt, tenantHeader), id);
        return ResponseEntity.ok(PropertyDetailResponse.from(property));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new property (AGENT, ADMIN)")
    public ResponseEntity<PropertyDetailResponse> createProperty(
            @Valid @RequestBody CreatePropertyRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        String username = jwt.getClaimAsString("preferred_username");

        CreatePropertyUseCase.Command cmd = new CreatePropertyUseCase.Command(
                tenantId,
                request.reference(),
                PropertyType.valueOf(request.type()),
                request.subtype(),
                new OwnerInfo(request.ownerType(), request.ownerId(), request.ownerName()),
                request.street(),
                request.streetNumber(),
                request.floor(),
                request.door(),
                request.postalCode(),
                new GeoPoint(request.lat(), request.lng()),
                new Surface(request.builtSqm(), request.usefulSqm(), request.plotSqm()),
                new Layout(request.rooms(), request.bathrooms(), request.terraces(),
                        request.parkingSpots(), request.storageRooms(),
                        request.propertyFloor(), request.hasElevator()),
                request.condition() != null ? PropertyCondition.valueOf(request.condition()) : null,
                request.buildYear(),
                null, // energyRating provided separately
                request.features() != null ? request.features() : java.util.Set.of(),
                java.util.Set.of(),
                request.tags() != null ? request.tags() : java.util.Set.of(),
                username);

        Property created = createPropertyUseCase.execute(cmd);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PropertyDetailResponse.from(created));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update property fields (AGENT owner or ADMIN)")
    public ResponseEntity<PropertyDetailResponse> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        // TODO: ownership check + partial update
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publish a property (validates invariants)")
    public ResponseEntity<PropertyDetailResponse> publishProperty(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        Property published = publishPropertyUseCase.execute(id, username);
        return ResponseEntity.ok(PropertyDetailResponse.from(published));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Archive a property")
    public ResponseEntity<PropertyDetailResponse> archiveProperty(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        Property archived = archivePropertyUseCase.execute(id, username);
        return ResponseEntity.ok(PropertyDetailResponse.from(archived));
    }

    private UUID tenantId(Jwt jwt) {
        return tenantId(jwt, null);
    }

    private UUID tenantId(Jwt jwt, UUID tenantHeader) {
        if (jwt == null || jwt.getClaimAsString("tenant_id") == null) {
            return tenantHeader;
        }
        return UUID.fromString(jwt.getClaimAsString("tenant_id"));
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Enum.valueOf(type, raw.trim().toUpperCase());
    }
}
