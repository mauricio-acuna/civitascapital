package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{id}/search-preferences")
@Tag(name = "SearchPreferences", description = "Búsquedas guardadas y alertas")
@RequiredArgsConstructor
public class SearchPreferenceController {

    private final SaveSearchPreferenceUseCase savePreference;
    private final com.magenta.customers.domain.port.out.SearchPreferenceRepository preferenceRepo;

    public record CreatePreferenceRequest(
            OperationType operationType,
            Set<String> propertyTypes,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Integer surfaceMin,
            Short roomsMin,
            Short bathroomsMin,
            Set<UUID> zoneIds,
            boolean requiresFiber,
            Integer maxRiskOccupation,
            AlertChannel alertChannel,
            AlertFrequency alertFrequency
    ) {}

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "UC-C7: Guardar búsqueda con alerta")
    public SearchPreference create(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePreferenceRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        return savePreference.execute(new SaveSearchPreferenceUseCase.Command(
                id, tenantId(jwt), req.operationType(), req.propertyTypes(),
                req.priceMin(), req.priceMax(), req.surfaceMin(),
                req.roomsMin(), req.bathroomsMin(), req.zoneIds(),
                req.requiresFiber(), req.maxRiskOccupation(),
                req.alertChannel(), req.alertFrequency(), jwt.getSubject()));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Listar búsquedas guardadas del cliente")
    public List<SearchPreference> list(@PathVariable UUID id) {
        return preferenceRepo.findActiveByCustomerId(id);
    }

    private UUID tenantId(Jwt jwt) {
        String tid = jwt.getClaimAsString("tenant_id");
        return tid != null ? UUID.fromString(tid) : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
