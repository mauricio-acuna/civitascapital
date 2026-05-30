package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.RequestPreapprovalUseCase;
import com.magenta.banks.application.usecase.UpdatePreapprovalStatusUseCase;
import com.magenta.banks.domain.model.PreapprovalStatus;
import com.magenta.banks.domain.model.preapproval.Preapproval;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preapprovals")
@Tag(name = "Preapprovals", description = "Pre-aprobaciones hipotecarias")
public class PreapprovalController {

    private final RequestPreapprovalUseCase      requestUseCase;
    private final UpdatePreapprovalStatusUseCase updateUseCase;

    public PreapprovalController(RequestPreapprovalUseCase requestUseCase,
                                 UpdatePreapprovalStatusUseCase updateUseCase) {
        this.requestUseCase = requestUseCase;
        this.updateUseCase  = updateUseCase;
    }

    public record PreapprovalRequest(
        @NotNull UUID productId,
        UUID propertyId,
        @NotNull BigDecimal amount,
        BigDecimal propertyPrice,
        @NotNull int termMonths
    ) {}

    public record StatusPatchRequest(
        @NotNull PreapprovalStatus status,
        String reason,
        List<String> conditions
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Solicitar pre-aprobación (UC-B5)")
    public Preapproval request(@Valid @RequestBody PreapprovalRequest req,
                               @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = customerId(jwt);
        return requestUseCase.execute(new RequestPreapprovalUseCase.Command(
                tenantId(jwt), customerId, req.productId(), req.propertyId(),
                req.amount(), req.propertyPrice(), req.termMonths()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','SYSTEM')")
    @Operation(summary = "Cambiar estado de pre-aprobación")
    public Preapproval updateStatus(@PathVariable UUID id,
                                    @Valid @RequestBody StatusPatchRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        String actor = jwt != null ? jwt.getClaimAsString("preferred_username") : "system";
        return updateUseCase.execute(new UpdatePreapprovalStatusUseCase.Command(
                id, req.status(), req.reason(), actor,
                req.conditions() != null ? req.conditions() : List.of()));
    }

    private UUID tenantId(Jwt jwt) {
        if (jwt == null) return null;
        String raw = jwt.getClaimAsString("tenant_id");
        return raw != null ? UUID.fromString(raw) : null;
    }

    private UUID customerId(Jwt jwt) {
        if (jwt == null) return null;
        String raw = jwt.getClaimAsString("customer_id");
        return raw != null ? UUID.fromString(raw) : null;
    }
}
