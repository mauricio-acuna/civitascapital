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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Properties", description = "Property catalogue management")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final PublishPropertyUseCase publishPropertyUseCase;
    private final ArchivePropertyUseCase archivePropertyUseCase;

    public PropertyController(CreatePropertyUseCase createPropertyUseCase,
                               PublishPropertyUseCase publishPropertyUseCase,
                               ArchivePropertyUseCase archivePropertyUseCase) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.publishPropertyUseCase = publishPropertyUseCase;
        this.archivePropertyUseCase = archivePropertyUseCase;
    }

    @GetMapping
    @Operation(summary = "List properties (basic filter)")
    public ResponseEntity<?> listProperties(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Delegated to SearchController for full faceted search
        // This endpoint is a lightweight listing for authenticated backoffice use
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property detail")
    public ResponseEntity<PropertyDetailResponse> getProperty(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        // TODO: look up property + check address visibility rules
        return ResponseEntity.ok().build();
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
}
