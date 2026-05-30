package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.CompareProductsUseCase;
import com.magenta.banks.application.usecase.SimulateNinetyFiveFiveUseCase;
import com.magenta.banks.application.usecase.SimulateLoanUseCase;
import com.magenta.banks.domain.model.ContractType;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import com.magenta.banks.domain.model.loansimulation.LoanSimulation;
import com.magenta.banks.infrastructure.adapter.in.web.dto.SimulationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulations")
@Tag(name = "Simulations", description = "Simulaciones de préstamo hipotecario")
public class LoanSimulationController {

    private final SimulateLoanUseCase  simulateUseCase;
    private final SimulateNinetyFiveFiveUseCase simulateNinetyFiveFiveUseCase;
    private final CompareProductsUseCase compareUseCase;

    public LoanSimulationController(SimulateLoanUseCase simulateUseCase,
                                    SimulateNinetyFiveFiveUseCase simulateNinetyFiveFiveUseCase,
                                    CompareProductsUseCase compareUseCase) {
        this.simulateUseCase = simulateUseCase;
        this.simulateNinetyFiveFiveUseCase = simulateNinetyFiveFiveUseCase;
        this.compareUseCase  = compareUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Simular préstamo (UC-B3)")
    public LoanSimulation simulate(@Valid @RequestBody SimulationRequest req,
                                   @AuthenticationPrincipal Jwt jwt) {
        return simulateUseCase.execute(toCommand(req, jwt));
    }

    @PostMapping("/90-5-5")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Simular esquema 90+5+5 (UC-B4)")
    public SimulateNinetyFiveFiveUseCase.Result simulateNinetyFiveFive(
            @Valid @RequestBody NinetyFiveFiveRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return simulateNinetyFiveFiveUseCase.execute(new SimulateNinetyFiveFiveUseCase.Command(
                tenantId(jwt),
                customerId(jwt),
                req.productId(),
                req.propertyId(),
                req.zoneId(),
                req.propertyPrice(),
                req.surfaceSqm(),
                req.propertyType(),
                req.termMonths(),
                toBorrower(req.borrower()),
                req.newBuild()));
    }

    @PostMapping("/compare")
    @Operation(summary = "Comparar 2-N productos (UC-B7)")
    public List<LoanSimulation> compare(@Valid @RequestBody CompareRequest req,
                                        @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId   = tenantId(jwt);
        UUID customerId = customerId(jwt);
        BorrowerProfile borrower = toBorrower(req.borrower());
        return compareUseCase.execute(new CompareProductsUseCase.Command(
                tenantId, customerId, req.productIds(), req.propertyId(), req.zoneId(),
                req.requestedAmount(), req.propertyPrice(), req.surfaceSqm(),
                req.propertyType(), req.operationType(), req.termMonths(), borrower));
    }

    public record CompareRequest(
        List<UUID> productIds,
        UUID propertyId,
        UUID zoneId,
        BigDecimal requestedAmount,
        BigDecimal propertyPrice,
        BigDecimal surfaceSqm,
        String propertyType,
        String operationType,
        int termMonths,
        SimulationRequest.BorrowerRequest borrower
    ) {}

    public record NinetyFiveFiveRequest(
        @jakarta.validation.constraints.NotNull UUID productId,
        UUID propertyId,
        UUID zoneId,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive BigDecimal propertyPrice,
        BigDecimal surfaceSqm,
        String propertyType,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(12) int termMonths,
        boolean newBuild,
        @jakarta.validation.constraints.NotNull SimulationRequest.BorrowerRequest borrower
    ) {}

    private SimulateLoanUseCase.Command toCommand(SimulationRequest req, Jwt jwt) {
        return new SimulateLoanUseCase.Command(
                tenantId(jwt), customerId(jwt), req.productId(), req.propertyId(),
                req.zoneId(), req.requestedAmount(), req.propertyPrice(), req.surfaceSqm(),
                req.propertyType(), req.operationType(), req.termMonths(), toBorrower(req.borrower()));
    }

    private BorrowerProfile toBorrower(SimulationRequest.BorrowerRequest b) {
        return new BorrowerProfile(b.netIncomeMonthly(),
                b.payments() > 0 ? b.payments() : 12,
                b.age(), ContractType.valueOf(b.contractType()),
                b.seniorityMonths(),
                b.otherDebtMonthly() != null ? b.otherDebtMonthly() : BigDecimal.ZERO,
                b.dependents(),
                b.ownFunds() != null ? b.ownFunds() : BigDecimal.ZERO,
                b.hasGuarantor(), false);
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
