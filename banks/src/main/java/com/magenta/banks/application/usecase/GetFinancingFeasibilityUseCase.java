package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.appraisal.Appraisal;
import com.magenta.banks.domain.model.loanproduct.EligibilityRules;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.domain.model.loansimulation.BorrowerProfile;
import com.magenta.banks.domain.model.loansimulation.EvaluationContext;
import com.magenta.banks.domain.model.loansimulation.TaxInfo;
import com.magenta.banks.domain.model.pagination.PageSpec;
import com.magenta.banks.domain.port.out.*;
import com.magenta.banks.domain.service.FrenchAmortizationService;
import com.magenta.banks.domain.service.RuleEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UC-B6: Determinar qué bancos pueden financiar una propiedad y para qué perfil.
 * Endpoint GET /financing-feasibility
 */
@Service
public class GetFinancingFeasibilityUseCase {

    private final LoanProductRepository productRepository;
    private final AppraisalRepository   appraisalRepository;
    private final PropertyClient        propertyClient;
    private final CustomerClient        customerClient;
    private final ZoneClient            zoneClient;
    private final RuleEngine            ruleEngine = new RuleEngine();
    private final FrenchAmortizationService french = new FrenchAmortizationService();

    public record FeasibilityResult(
        UUID productId,
        String productName,
        UUID bankId,
        BigDecimal ltvMax,
        BigDecimal monthlyPayment,
        BigDecimal effortRatio,
        String verdict,
        boolean eligible
    ) {}

    public GetFinancingFeasibilityUseCase(LoanProductRepository productRepository,
                                          AppraisalRepository appraisalRepository,
                                          PropertyClient propertyClient,
                                          CustomerClient customerClient,
                                          ZoneClient zoneClient) {
        this.productRepository  = productRepository;
        this.appraisalRepository = appraisalRepository;
        this.propertyClient     = propertyClient;
        this.customerClient     = customerClient;
        this.zoneClient         = zoneClient;
    }

    @Transactional(readOnly = true)
    public List<FeasibilityResult> execute(UUID tenantId, UUID propertyId, UUID customerId) {
        PropertyClient.PropertyInfo property = propertyClient.getPropertyInfo(propertyId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Property not found: " + propertyId));

        CustomerClient.FinancialProfile profile = customerClient.getFinancialProfile(customerId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Customer not found: " + customerId));

        Optional<Appraisal> appraisal = appraisalRepository.findLatestValidByPropertyId(propertyId);
        BigDecimal price = appraisal.map(Appraisal::mortgageValue).orElse(property.price());

        BorrowerProfile borrower = new BorrowerProfile(
                profile.netIncomeMonthly(), profile.payments(), profile.age(),
                com.magenta.banks.domain.model.ContractType.valueOf(profile.contractType()),
                profile.seniorityMonths(), profile.otherDebtMonthly(),
                profile.dependents(), BigDecimal.ZERO, false, profile.cirbeFlag());

        // Buscar productos activos de cualquier banco
        var products = productRepository.search(tenantId, null, null, null, price, null,
                PageSpec.of(0, 100));

        return products.content().stream().map(p -> evaluate(p, price, borrower)).collect(Collectors.toList());
    }

    private FeasibilityResult evaluate(LoanProduct product, BigDecimal price, BorrowerProfile borrower) {
        BigDecimal ltvMax = product.ltvMaxPct().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal loanAmount = price.multiply(ltvMax).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthly = french.monthlyPayment(loanAmount, product.rateInfo().initialPct(), 360);
        BigDecimal effortRatio = monthly.divide(borrower.netIncomeMonthly(), 4, RoundingMode.HALF_UP);
        BigDecimal debtRatio = monthly.add(borrower.otherDebtMonthly())
                .divide(borrower.netIncomeMonthly(), 4, RoundingMode.HALF_UP);

        EvaluationContext ctx = new EvaluationContext(borrower, 360, effortRatio, debtRatio, ltvMax);
        boolean eligible = ruleEngine.evaluate(product.eligibility(), ctx);

        String verdict = eligible
                ? (effortRatio.doubleValue() <= 0.35 ? "APPROVABLE" : "TIGHT")
                : "REJECTABLE";

        return new FeasibilityResult(product.id(), product.name(), product.bankId(),
                product.ltvMaxPct(), monthly, effortRatio, verdict, eligible);
    }
}
