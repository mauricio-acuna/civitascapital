package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.GetFinancingFeasibilityUseCase;
import com.magenta.banks.application.usecase.MarkPropertyFinanciableUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financing-feasibility")
@Tag(name = "Feasibility", description = "Viabilidad de financiación")
public class FeasibilityController {

    private final GetFinancingFeasibilityUseCase feasibilityUseCase;
    private final MarkPropertyFinanciableUseCase markPropertyFinanciableUseCase;

    public FeasibilityController(GetFinancingFeasibilityUseCase feasibilityUseCase,
                                 MarkPropertyFinanciableUseCase markPropertyFinanciableUseCase) {
        this.feasibilityUseCase = feasibilityUseCase;
        this.markPropertyFinanciableUseCase = markPropertyFinanciableUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','SYSTEM')")
    @Operation(summary = "Viabilidad de financiación para propiedad+cliente o badge por inmueble (UC-B6)")
    public Object get(
            @RequestParam UUID propertyId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) BigDecimal price,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = jwt != null ? UUID.fromString(jwt.getClaimAsString("tenant_id")) : null;
        if (customerId == null) {
            return markPropertyFinanciableUseCase.execute(
                    new MarkPropertyFinanciableUseCase.Command(tenantId, propertyId, price));
        }
        return feasibilityUseCase.execute(tenantId, propertyId, customerId);
    }
}
