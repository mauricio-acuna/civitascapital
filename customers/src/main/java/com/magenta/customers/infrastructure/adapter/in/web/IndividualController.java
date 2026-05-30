package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Gestión de clientes")
@RequiredArgsConstructor
public class IndividualController {

    private final RegisterIndividualUseCase registerIndividual;

    // ── Request DTOs ────────────────────────────────────────

    public record RegisterIndividualRequest(
            @NotBlank String nif,
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 160) String lastName,
            @NotNull LocalDate birthDate,
            String nationality,
            String residenceCountry,
            String taxResidence,
            String civilStatus,
            String phone,
            @Email String email,
            PostalAddress address,
            ProfessionalProfile professional
    ) {}

    @PostMapping("/individuals")
    @Operation(summary = "UC-C1: Registrar persona física")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse registerIndividual(
            @Valid @RequestBody RegisterIndividualRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        UUID tenantId = tenantIdFrom(jwt);
        String requestedBy = jwt != null ? jwt.getSubject() : "anonymous";

        Customer customer = registerIndividual.execute(new RegisterIndividualUseCase.Command(
                tenantId, req.nif(), req.firstName(), req.lastName(),
                req.birthDate(), req.nationality(), req.residenceCountry(),
                req.taxResidence(), req.civilStatus(), req.phone(), req.email(),
                req.address(), req.professional(), requestedBy));

        return CustomerResponse.from(customer);
    }

    // ── Helpers ─────────────────────────────────────────────

    private UUID tenantIdFrom(Jwt jwt) {
        if (jwt == null) return UUID.fromString("00000000-0000-0000-0000-000000000001");
        String tid = jwt.getClaimAsString("tenant_id");
        return tid != null ? UUID.fromString(tid) : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
