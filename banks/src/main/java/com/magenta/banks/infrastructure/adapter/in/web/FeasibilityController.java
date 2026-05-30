package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.GetFinancingFeasibilityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financing-feasibility")
@Tag(name = "Feasibility", description = "Viabilidad de financiación")
public class FeasibilityController {

    private final GetFinancingFeasibilityUseCase feasibilityUseCase;

    public FeasibilityController(GetFinancingFeasibilityUseCase feasibilityUseCase) {
        this.feasibilityUseCase = feasibilityUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT')")
    @Operation(summary = "Viabilidad de financiación para propiedad+cliente (UC-B6)")
    public List<GetFinancingFeasibilityUseCase.FeasibilityResult> get(
            @RequestParam UUID propertyId,
            @RequestParam UUID customerId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = jwt != null ? UUID.fromString(jwt.getClaimAsString("tenant_id")) : null;
        return feasibilityUseCase.execute(tenantId, propertyId, customerId);
    }
}
