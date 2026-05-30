package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.Consent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{id}/consents")
@Tag(name = "Consents", description = "Consentimientos RGPD")
@RequiredArgsConstructor
public class ConsentController {

    private final ManageConsentUseCase manageConsent;
    private final com.magenta.customers.domain.port.out.ConsentRepository consentRepo;

    public record GrantConsentRequest(
            String purpose,
            String legalBasis,
            Map<String, Object> evidence
    ) {}

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Otorgar consentimiento")
    public Consent grant(
            @PathVariable UUID id,
            @RequestBody GrantConsentRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        return manageConsent.grant(new ManageConsentUseCase.GrantCommand(
                id, tenantId(jwt), req.purpose(), req.legalBasis(),
                req.evidence(), jwt.getSubject()));
    }

    @DeleteMapping("/{purpose}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Revocar consentimiento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID id,
            @PathVariable String purpose,
            @AuthenticationPrincipal Jwt jwt) {

        manageConsent.revoke(new ManageConsentUseCase.RevokeCommand(
                id, tenantId(jwt), purpose, jwt.getSubject()));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Listar consentimientos del cliente")
    public List<Consent> list(@PathVariable UUID id) {
        return consentRepo.findByCustomerId(id);
    }

    private UUID tenantId(Jwt jwt) {
        String tid = jwt.getClaimAsString("tenant_id");
        return tid != null ? UUID.fromString(tid) : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
