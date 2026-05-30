package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.in.AffordabilityPort;
import com.magenta.customers.domain.port.out.FinancialSnapshotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{id}")
@Tag(name = "Financial", description = "Perfil financiero y capacidad hipotecaria")
@RequiredArgsConstructor
public class FinancialController {

    private final SaveFinancialSnapshotUseCase saveSnapshot;
    private final FinancialSnapshotRepository snapshotRepo;
    private final ShareProfileWithBankUseCase shareProfile;
    private final AffordabilityPort affordability;

    public record SnapshotRequest(
            LocalDate asOf,
            BigDecimal netIncomeMonthly,
            int payments,
            BigDecimal grossIncomeAnnual,
            BigDecimal otherDebtMonthly,
            boolean cirbeFlag,
            BigDecimal ownFunds,
            int existingProperties,
            BigDecimal rentalIncomeMonthly
    ) {}

    @PostMapping("/financial-snapshots")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "UC-C4: Guardar perfil financiero")
    public FinancialSnapshot saveSnapshot(
            @PathVariable UUID id,
            @Valid @RequestBody SnapshotRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        return saveSnapshot.execute(new SaveFinancialSnapshotUseCase.Command(
                id, tenantId(jwt), req.asOf(), req.netIncomeMonthly(),
                req.payments(), req.grossIncomeAnnual(), req.otherDebtMonthly(),
                req.cirbeFlag(), req.ownFunds(), req.existingProperties(),
                req.rentalIncomeMonthly(), jwt.getSubject()));
    }

    @GetMapping("/financial-profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_OFFICER') or hasRole('AGENT')")
    @Cacheable(value = "financialProfile", key = "#id")
    @Operation(summary = "Obtener perfil financiero (DTO para banks)")
    public FinancialSnapshot getFinancialProfile(@PathVariable UUID id) {
        return snapshotRepo.findLatestByCustomerId(id)
                .orElseThrow(() -> new IllegalStateException("No financial profile for customer " + id));
    }

    @GetMapping("/affordability")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "UC-C10: Calcular capacidad hipotecaria")
    public ComputedAffordability getAffordability(@PathVariable UUID id) {
        FinancialSnapshot snapshot = snapshotRepo.findLatestByCustomerId(id)
                .orElseThrow(() -> new IllegalStateException("No financial profile for customer " + id));
        return affordability.compute(snapshot);
    }

    @PostMapping("/share-with-bank")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "UC-C11: Compartir perfil con banco")
    public FinancialSnapshot shareWithBank(@PathVariable UUID id,
                                           @RequestBody(required = false) ShareRequest req) {
        UUID bankId = req != null ? req.bankId() : null;
        return shareProfile.execute(new ShareProfileWithBankUseCase.Command(id, bankId));
    }

    public record ShareRequest(UUID bankId) {}

    private UUID tenantId(Jwt jwt) {
        String tid = jwt.getClaimAsString("tenant_id");
        return tid != null ? UUID.fromString(tid) : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
