package com.magenta.servicios.infrastructure.adapter.in.web;

import com.magenta.servicios.domain.model.Partner;
import com.magenta.servicios.domain.model.PartnerKind;
import com.magenta.servicios.domain.model.ServiceCode;
import com.magenta.servicios.domain.port.out.PartnerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partners")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Partners", description = "Gestión de proveedores de servicios")
public class PartnersController {

    private final PartnerRepository partnerRepository;

    public PartnersController(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @PostMapping
    @Operation(summary = "Crear partner")
    public ResponseEntity<Partner> create(@Valid @RequestBody CreatePartnerRequest req,
                                           @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        Partner partner = new Partner(UUID.randomUUID(), tenantId,
                req.code(), req.name(), req.kind(),
                req.services() != null ? Set.copyOf(req.services()) : Set.of(),
                Set.of(), req.commissionPct(), null, null, true, null, Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerRepository.save(partner));
    }

    @GetMapping
    @Operation(summary = "Listar partners")
    public ResponseEntity<List<Partner>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return ResponseEntity.ok(partnerRepository.findAll(tenantId));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar partner (activar/desactivar)")
    public ResponseEntity<Void> patch(@PathVariable UUID id,
                                       @RequestBody PatchPartnerRequest req) {
        partnerRepository.findById(id).ifPresent(p -> {
            if (Boolean.TRUE.equals(req.active())) p.activate();
            else if (Boolean.FALSE.equals(req.active())) p.deactivate();
            partnerRepository.save(p);
        });
        return ResponseEntity.noContent().build();
    }

    public record CreatePartnerRequest(
            @NotBlank String code, @NotBlank String name,
            @NotNull PartnerKind kind,
            List<ServiceCode> services, BigDecimal commissionPct) {}

    public record PatchPartnerRequest(Boolean active) {}
}
