package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import com.magenta.banks.domain.model.loansimulation.LoanSimulation;
import com.magenta.banks.domain.model.loansimulation.TaxInfo;
import com.magenta.banks.domain.port.out.LoanProductRepository;
import com.magenta.banks.domain.port.out.ZoneClient;
import com.magenta.banks.domain.service.NinetyFiveFiveBreakdownService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * UC-B4: Simular esquema 90+5+5.
 *
 * Convierte el precio de compraventa en el importe bancario esperado del 90%,
 * calcula el desglose comercial 90+5+5 y delega cuota, TAE, ratios y scoring en
 * la simulacion hipotecaria general.
 */
@Service
public class SimulateNinetyFiveFiveUseCase {

    private final LoanProductRepository productRepository;
    private final ZoneClient zoneClient;
    private final SimulateLoanUseCase simulateLoanUseCase;
    private final NinetyFiveFiveBreakdownService breakdownService = new NinetyFiveFiveBreakdownService();

    public SimulateNinetyFiveFiveUseCase(LoanProductRepository productRepository,
                                         ZoneClient zoneClient,
                                         SimulateLoanUseCase simulateLoanUseCase) {
        this.productRepository = productRepository;
        this.zoneClient = zoneClient;
        this.simulateLoanUseCase = simulateLoanUseCase;
    }

    public record Command(
            UUID tenantId,
            UUID customerId,
            UUID productId,
            UUID propertyId,
            UUID zoneId,
            BigDecimal propertyPrice,
            BigDecimal surfaceSqm,
            String propertyType,
            int termMonths,
            BorrowerProfile borrower,
            boolean newBuild
    ) {}

    public record Result(
            LoanSimulation simulation,
            NinetyFiveFiveBreakdownService.Breakdown breakdown
    ) {}

    @Transactional
    public Result execute(Command cmd) {
        LoanProduct product = productRepository.findById(cmd.productId())
                .orElseThrow(() -> new NoSuchElementException("LoanProduct not found: " + cmd.productId()));

        if (product.scheme() != Scheme.NINETY_FIVE_FIVE) {
            throw new IllegalArgumentException("LoanProduct is not configured for NINETY_FIVE_FIVE scheme");
        }

        TaxInfo taxes = resolveTaxes(cmd.zoneId());
        NinetyFiveFiveBreakdownService.Breakdown breakdown =
                breakdownService.calculate(cmd.propertyPrice(), taxes, cmd.newBuild());

        LoanSimulation simulation = simulateLoanUseCase.execute(new SimulateLoanUseCase.Command(
                cmd.tenantId(),
                cmd.customerId(),
                cmd.productId(),
                cmd.propertyId(),
                cmd.zoneId(),
                breakdown.bankLoan(),
                cmd.propertyPrice(),
                cmd.surfaceSqm(),
                cmd.newBuild() ? "NEW_BUILD_" + safePropertyType(cmd.propertyType()) : safePropertyType(cmd.propertyType()),
                "SALE",
                cmd.termMonths(),
                cmd.borrower()));

        return new Result(simulation, breakdown);
    }

    private TaxInfo resolveTaxes(UUID zoneId) {
        if (zoneId == null) {
            return new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null);
        }
        return zoneClient.getZoneInfo(zoneId)
                .map(z -> new TaxInfo(z.ivaPct(), z.ajdPct(), z.itpPct()))
                .orElseGet(() -> new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null));
    }

    private String safePropertyType(String propertyType) {
        return propertyType == null || propertyType.isBlank() ? "RESIDENTIAL" : propertyType;
    }
}

