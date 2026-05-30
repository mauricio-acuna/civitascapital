package com.magenta.products.infrastructure.adapter.in.web;

import com.magenta.products.application.MatchAffordablePropertiesUseCase;
import com.magenta.products.domain.service.PropertyAffordabilityMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/property-matches")
@Tag(name = "Property Matches", description = "Matching financiero de inmuebles para Civitas Pro")
public class PropertyMatchController {

    private final MatchAffordablePropertiesUseCase matchAffordablePropertiesUseCase;

    public PropertyMatchController(MatchAffordablePropertiesUseCase matchAffordablePropertiesUseCase) {
        this.matchAffordablePropertiesUseCase = matchAffordablePropertiesUseCase;
    }

    @PostMapping("/affordability")
    @Operation(summary = "Buscar inmuebles por encaje financiero")
    public List<AffordablePropertyMatchResponse> matchByAffordability(
            @Valid @RequestBody AffordablePropertyMatchRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return matchAffordablePropertiesUseCase.execute(new MatchAffordablePropertiesUseCase.Command(
                        tenantId(jwt),
                        request.maxTicket(),
                        request.zoneIds(),
                        request.roomsMin()))
                .stream()
                .map(AffordablePropertyMatchResponse::from)
                .toList();
    }

    public record AffordablePropertyMatchRequest(
            @NotNull @Positive BigDecimal maxTicket,
            @NotNull Set<UUID> zoneIds,
            Integer roomsMin
    ) {}

    public record AffordablePropertyMatchResponse(
            UUID propertyId,
            String reference,
            UUID zoneId,
            BigDecimal price,
            String status,
            BigDecimal priceGap,
            BigDecimal priceToBudgetRatio
    ) {
        static AffordablePropertyMatchResponse from(PropertyAffordabilityMatchService.Match match) {
            return new AffordablePropertyMatchResponse(
                    match.property().id(),
                    match.property().reference(),
                    match.property().location().zoneId(),
                    match.operation() != null ? match.operation().price().amount() : null,
                    match.status().name(),
                    match.priceGap(),
                    match.priceToBudgetRatio());
        }
    }

    private UUID tenantId(Jwt jwt) {
        if (jwt == null) return null;
        String raw = jwt.getClaimAsString("tenant_id");
        return raw != null ? UUID.fromString(raw) : null;
    }
}

