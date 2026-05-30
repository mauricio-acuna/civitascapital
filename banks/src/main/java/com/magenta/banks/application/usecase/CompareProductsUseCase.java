package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.loansimulation.LoanSimulation;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.port.out.LoanProductRepository;
import com.magenta.banks.domain.port.out.LoanSimulationRepository;
import com.magenta.banks.domain.port.out.ZoneClient;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UC-B7: Comparar 2-N productos hipotecarios devolviendo simulaciones paralelas.
 */
@Service
public class CompareProductsUseCase {

    private final SimulateLoanUseCase simulateLoanUseCase;

    public CompareProductsUseCase(SimulateLoanUseCase simulateLoanUseCase) {
        this.simulateLoanUseCase = simulateLoanUseCase;
    }

    public record Command(
        UUID tenantId,
        UUID customerId,
        List<UUID> productIds,
        UUID propertyId,
        UUID zoneId,
        java.math.BigDecimal requestedAmount,
        java.math.BigDecimal propertyPrice,
        java.math.BigDecimal surfaceSqm,
        String propertyType,
        String operationType,
        int termMonths,
        BorrowerProfile borrower
    ) {}

    @Transactional
    public List<LoanSimulation> execute(Command cmd) {
        return cmd.productIds().stream()
                .map(productId -> simulateLoanUseCase.execute(new SimulateLoanUseCase.Command(
                        cmd.tenantId(), cmd.customerId(), productId,
                        cmd.propertyId(), cmd.zoneId(), cmd.requestedAmount(),
                        cmd.propertyPrice(), cmd.surfaceSqm(), cmd.propertyType(),
                        cmd.operationType(), cmd.termMonths(), cmd.borrower())))
                .collect(Collectors.toList());
    }
}
