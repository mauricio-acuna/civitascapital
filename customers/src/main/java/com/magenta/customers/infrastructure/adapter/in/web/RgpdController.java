package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{id}/rgpd")
@Tag(name = "RGPD", description = "Derechos RGPD del cliente")
@RequiredArgsConstructor
public class RgpdController {

    private final RgpdExportUseCase exportUseCase;
    private final RgpdErasureUseCase erasureUseCase;

    @PostMapping("/export")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "UC-C9: Exportar datos RGPD (portabilidad)")
    public RgpdExportUseCase.Result export(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        return exportUseCase.execute(new RgpdExportUseCase.Command(id, tenantId(jwt)));
    }

    @PostMapping("/erasure")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "UC-C9: Solicitar borrado (crypto-shredding + tombstone)")
    public void erasure(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        erasureUseCase.execute(new RgpdErasureUseCase.Command(id, tenantId(jwt), jwt.getSubject()));
    }

    private UUID tenantId(Jwt jwt) {
        String tid = jwt.getClaimAsString("tenant_id");
        return tid != null ? UUID.fromString(tid) : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
