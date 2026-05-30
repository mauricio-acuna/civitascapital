package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.GetAppraisalUseCase;
import com.magenta.banks.application.usecase.RegisterAppraisalUseCase;
import com.magenta.banks.domain.model.appraisal.Appraisal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appraisals")
@Tag(name = "Appraisals", description = "Tasaciones ECO/805-2003")
public class AppraisalController {

    private final RegisterAppraisalUseCase registerUseCase;
    private final GetAppraisalUseCase getAppraisalUseCase;

    public AppraisalController(RegisterAppraisalUseCase registerUseCase,
                               GetAppraisalUseCase getAppraisalUseCase) {
        this.registerUseCase = registerUseCase;
        this.getAppraisalUseCase = getAppraisalUseCase;
    }

    public record AppraisalRequest(
        @NotNull UUID propertyId,
        UUID customerId,
        @NotNull UUID providerId,
        @NotNull @Positive BigDecimal marketValue,
        @NotNull @Positive BigDecimal mortgageValue,
        @NotNull @Positive BigDecimal surfaceSqm,
        @NotNull LocalDate issuedAt,
        String pdfUrl
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BANK_OFFICER')")
    @Operation(summary = "Registrar tasación (UC-B10)")
    public Appraisal register(@Valid @RequestBody AppraisalRequest req,
                              @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = jwt != null ? UUID.fromString(jwt.getClaimAsString("tenant_id")) : null;
        return registerUseCase.execute(new RegisterAppraisalUseCase.Command(
                tenantId, req.propertyId(), req.customerId(), req.providerId(),
                req.marketValue(), req.mortgageValue(), req.surfaceSqm(),
                req.issuedAt(), req.pdfUrl()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','ADMIN','SYSTEM')")
    @Operation(summary = "Obtener tasación por ID")
    public Appraisal getById(@PathVariable UUID id) {
        return getAppraisalUseCase.byId(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BANK_OFFICER','ADMIN','SYSTEM')")
    @Operation(summary = "Buscar tasaciones por inmueble o cliente")
    public List<Appraisal> list(@RequestParam(required = false) UUID propertyId,
                                @RequestParam(required = false) UUID customerId) {
        if (propertyId != null) {
            return getAppraisalUseCase.byProperty(propertyId);
        }
        if (customerId != null) {
            return getAppraisalUseCase.byCustomer(customerId);
        }
        throw new IllegalArgumentException("propertyId or customerId is required");
    }

    @GetMapping("/latest-valid")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','ADMIN','SYSTEM')")
    @Operation(summary = "Obtener última tasación vigente de un inmueble")
    public Appraisal latestValid(@RequestParam UUID propertyId) {
        return getAppraisalUseCase.latestValidByProperty(propertyId);
    }
}
