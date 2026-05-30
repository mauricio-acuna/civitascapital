package com.magenta.servicios.infrastructure.adapter.in.web;

import com.magenta.servicios.application.usecase.SlaDashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sla-dashboard")
@Tag(name = "SLA Dashboard", description = "Métricas de SLA y satisfacción por proveedor")
public class SlaDashboardController {

    private final SlaDashboardQueryService queryService;

    public SlaDashboardController(SlaDashboardQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard SLA (UC-S10)")
    public ResponseEntity<SlaDashboardQueryService.DashboardData> dashboard(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return ResponseEntity.ok(queryService.execute(tenantId, pageable));
    }
}
