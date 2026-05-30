package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.event.SimulationCreated;
import com.magenta.banks.domain.model.loansimulation.*;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.port.out.*;
import com.magenta.banks.domain.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * UC-B3: Simular cuota e ingresos requeridos.
 * UC-B4: variante 90+5+5 (delegada al mismo servicio con flag scheme).
 */
@Service
public class SimulateLoanUseCase {

    private final LoanProductRepository     productRepository;
    private final LoanSimulationRepository  simulationRepository;
    private final ZoneClient                zoneClient;
    private final DomainEventPublisher      eventPublisher;
    private final FrenchAmortizationService french   = new FrenchAmortizationService();
    private final TaeCalculatorService      taeCalc  = new TaeCalculatorService();
    private final OwnFundsCalculatorService ownFunds = new OwnFundsCalculatorService();
    private final ApprovabilityScorerService scorer  = new ApprovabilityScorerService();

    public SimulateLoanUseCase(LoanProductRepository productRepository,
                               LoanSimulationRepository simulationRepository,
                               ZoneClient zoneClient,
                               DomainEventPublisher eventPublisher) {
        this.productRepository    = productRepository;
        this.simulationRepository = simulationRepository;
        this.zoneClient           = zoneClient;
        this.eventPublisher       = eventPublisher;
    }

    public record Command(
        UUID tenantId,
        UUID customerId,
        UUID productId,
        UUID propertyId,
        UUID zoneId,
        BigDecimal requestedAmount,
        BigDecimal propertyPrice,
        BigDecimal surfaceSqm,
        String propertyType,
        String operationType,
        int termMonths,
        BorrowerProfile borrower
    ) {}

    @Transactional
    public LoanSimulation execute(Command cmd) {
        LoanProduct product = productRepository.findById(cmd.productId())
                .orElseThrow(() -> new NoSuchElementException("LoanProduct not found: " + cmd.productId()));

        // Obtener tipos impositivos de la CCAA
        TaxInfo taxes = resolveTaxes(cmd.zoneId());

        // Calcular cuota (TIN fijo o inicial del producto)
        BigDecimal tin = product.rateInfo().initialPct();
        BigDecimal monthly = french.monthlyPayment(cmd.requestedAmount(), tin, cmd.termMonths());

        // Ratios
        BigDecimal effortRatio = monthly.divide(cmd.borrower().netIncomeMonthly(), 4, RoundingMode.HALF_UP);
        BigDecimal totalDebt   = monthly.add(cmd.borrower().otherDebtMonthly());
        BigDecimal debtRatio   = totalDebt.divide(cmd.borrower().netIncomeMonthly(), 4, RoundingMode.HALF_UP);

        // LTV
        BigDecimal ltvComputed = cmd.propertyPrice() != null && cmd.propertyPrice().signum() > 0
                ? cmd.requestedAmount().divide(cmd.propertyPrice(), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // TAE
        BigDecimal tae = taeCalc.calculateMortgageTae(
                cmd.requestedAmount(), tin, cmd.termMonths(),
                product.feeOpeningPct() != null ? product.feeOpeningPct() : BigDecimal.ZERO,
                BigDecimal.ZERO);

        // Fondos propios
        boolean isNewBuild = "SALE".equals(cmd.operationType()) && cmd.propertyType() != null
                && cmd.propertyType().contains("NEW");
        boolean is90Plus5 = product.scheme() != null
                && product.scheme().name().equals("NINETY_FIVE_FIVE");
        BigDecimal required = BigDecimal.ZERO;
        if (cmd.propertyPrice() != null && cmd.propertyPrice().signum() > 0) {
            required = ownFunds.calculate(cmd.propertyPrice(), ltvComputed, taxes, isNewBuild, is90Plus5);
        }
        BigDecimal gap = ownFunds.fundsGap(required, cmd.borrower().ownFunds());

        // Score
        ApprovabilityScorerService.ScoreResult sr = scorer.score(
                effortRatio, debtRatio, gap, cmd.propertyPrice(), cmd.borrower(), cmd.termMonths());

        // Warnings
        List<String> warnings = buildWarnings(effortRatio, debtRatio, gap, cmd.borrower(), cmd.termMonths());

        SimulationResult result = new SimulationResult(
                monthly, tae, tin,
                french.totalCost(cmd.requestedAmount(), tin, cmd.termMonths()),
                french.totalInterest(cmd.requestedAmount(), tin, cmd.termMonths()),
                effortRatio, debtRatio, ltvComputed, taxes,
                required, gap,
                sr.score(), sr.verdict(),
                warnings, List.of());

        LoanSimulation simulation = new LoanSimulation(
                UUID.randomUUID(), cmd.tenantId(), cmd.customerId(),
                cmd.productId(), cmd.propertyId(), cmd.zoneId(),
                cmd.requestedAmount(), cmd.propertyPrice(), cmd.surfaceSqm(),
                cmd.propertyType(), cmd.operationType(), cmd.termMonths(),
                cmd.borrower(), taxes, result, Instant.now());

        LoanSimulation saved = simulationRepository.save(simulation);

        eventPublisher.publish(new SimulationCreated(
                cmd.tenantId(), saved.id(), cmd.customerId(), cmd.productId()));

        return saved;
    }

    private TaxInfo resolveTaxes(UUID zoneId) {
        if (zoneId == null) return new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null);
        return zoneClient.getZoneInfo(zoneId)
                .map(z -> new TaxInfo(z.ivaPct(), z.ajdPct(), z.itpPct()))
                .orElseGet(() -> new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null));
    }

    private List<String> buildWarnings(BigDecimal effortRatio, BigDecimal debtRatio,
                                        BigDecimal gap, BorrowerProfile borrower, int termMonths) {
        List<String> w = new ArrayList<>();
        if (effortRatio.doubleValue() > 0.35)
            w.add("Ratio de esfuerzo supera el 35 % recomendado por el Banco de España.");
        if (debtRatio.doubleValue() > 0.40)
            w.add("Ratio de endeudamiento total supera el 40 %.");
        if (gap != null && gap.signum() > 0)
            w.add("Fondos propios insuficientes para escritura (faltan %,.0f €).".formatted(gap.doubleValue()));
        if (borrower.age() > 35)
            w.add("Sin Hipoteca Joven: edad supera 35 años.");
        if (borrower.cirbeFlag())
            w.add("Hay incidencias en CIRBE: el banco puede denegar la operación.");
        if (borrower.ageAtTermEnd(termMonths) > 70)
            w.add("La edad al vencimiento supera los 70 años.");
        return w;
    }
}
