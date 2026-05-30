package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.event.PreapprovalRequested;
import com.magenta.banks.domain.model.PreapprovalStatus;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import com.magenta.banks.domain.model.loansimulation.EvaluationContext;
import com.magenta.banks.domain.model.preapproval.Preapproval;
import com.magenta.banks.domain.port.out.*;
import com.magenta.banks.domain.service.RuleEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * UC-B5: Solicitar pre-aprobación hipotecaria.
 */
@Service
public class RequestPreapprovalUseCase {

    private final LoanProductRepository productRepository;
    private final PreapprovalRepository preapprovalRepository;
    private final CustomerClient        customerClient;
    private final DomainEventPublisher  eventPublisher;
    private final RuleEngine            ruleEngine = new RuleEngine();

    public RequestPreapprovalUseCase(LoanProductRepository productRepository,
                                     PreapprovalRepository preapprovalRepository,
                                     CustomerClient customerClient,
                                     DomainEventPublisher eventPublisher) {
        this.productRepository     = productRepository;
        this.preapprovalRepository = preapprovalRepository;
        this.customerClient        = customerClient;
        this.eventPublisher        = eventPublisher;
    }

    public record Command(
        UUID tenantId,
        UUID customerId,
        UUID productId,
        UUID propertyId,
        BigDecimal amount,
        BigDecimal propertyPrice,
        int termMonths
    ) {}

    @Transactional
    public Preapproval execute(Command cmd) {
        // Validar KYC del cliente
        CustomerClient.FinancialProfile profile = customerClient
                .getFinancialProfile(cmd.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + cmd.customerId()));

        if (!profile.kycApproved()) {
            throw new IllegalStateException("Customer KYC not approved: " + cmd.customerId());
        }

        LoanProduct product = productRepository.findById(cmd.productId())
                .orElseThrow(() -> new NoSuchElementException("LoanProduct not found: " + cmd.productId()));

        if (!product.isActive()) {
            throw new IllegalStateException("Product is not active: " + cmd.productId());
        }

        // Calcular LTV
        BigDecimal ltv = cmd.propertyPrice() != null && cmd.propertyPrice().signum() > 0
                ? cmd.amount().divide(cmd.propertyPrice(), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Evaluar elegibilidad
        BorrowerProfile borrower = new BorrowerProfile(
                profile.netIncomeMonthly(), profile.payments(), profile.age(),
                com.magenta.banks.domain.model.ContractType.valueOf(profile.contractType()),
                profile.seniorityMonths(), profile.otherDebtMonthly(), profile.dependents(),
                BigDecimal.ZERO, false, profile.cirbeFlag());

        // Ratios aproximados para la evaluación (cuota estimada)
        var french = new com.magenta.banks.domain.service.FrenchAmortizationService();
        BigDecimal monthly = french.monthlyPayment(cmd.amount(), product.rateInfo().initialPct(), cmd.termMonths());
        BigDecimal effortRatio = monthly.divide(profile.netIncomeMonthly(), 4, RoundingMode.HALF_UP);
        BigDecimal debtRatio = monthly.add(profile.otherDebtMonthly())
                .divide(profile.netIncomeMonthly(), 4, RoundingMode.HALF_UP);

        EvaluationContext ctx = new EvaluationContext(borrower, cmd.termMonths(), effortRatio, debtRatio, ltv);

        if (!ruleEngine.evaluate(product.eligibility(), ctx)) {
            throw new IllegalStateException("Borrower does not meet eligibility rules for product: " + cmd.productId());
        }

        Instant expiresAt = Instant.now().plus(90, ChronoUnit.DAYS);

        Preapproval preapproval = new Preapproval(
                UUID.randomUUID(), cmd.tenantId(), cmd.customerId(), cmd.productId(),
                cmd.propertyId(), cmd.amount(), cmd.termMonths(), ltv,
                PreapprovalStatus.REQUESTED, List.of(), expiresAt,
                Instant.now(), Instant.now(), List.of(), 0L);

        Preapproval saved = preapprovalRepository.save(preapproval);

        eventPublisher.publish(new PreapprovalRequested(
                cmd.tenantId(), saved.id(), cmd.customerId(), cmd.productId(),
                cmd.propertyId(), cmd.amount(), ltv, expiresAt));

        return saved;
    }
}
