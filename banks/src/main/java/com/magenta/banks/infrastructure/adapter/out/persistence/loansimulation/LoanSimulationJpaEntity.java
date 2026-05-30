package com.magenta.banks.infrastructure.adapter.out.persistence.loansimulation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_simulations", schema = "banks")
@Getter @Setter @NoArgsConstructor
public class LoanSimulationJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "bank_id", nullable = false)
    private UUID bankId;

    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "price", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "loan_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "tin_applied_pct", nullable = false, precision = 6, scale = 4)
    private BigDecimal tinAppliedPct;

    @Column(name = "monthly_payment", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "tae_pct", nullable = false, precision = 6, scale = 4)
    private BigDecimal taePct;

    @Column(name = "total_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "total_interest", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "effort_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal effortRatio;

    @Column(name = "debt_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal debtRatio;

    @Column(name = "ltv_computed", nullable = false, precision = 5, scale = 4)
    private BigDecimal ltvComputed;

    @Column(name = "approvability_score", nullable = false)
    private Integer approvabilityScore;

    @Column(name = "verdict", nullable = false, length = 20)
    private String verdict;

    @Column(name = "warnings", columnDefinition = "text[]")
    private String warnings; // stored as JSON array string

    @Column(name = "required_own_funds", precision = 14, scale = 2)
    private BigDecimal requiredOwnFunds;

    @Column(name = "funds_gap", precision = 14, scale = 2)
    private BigDecimal fundsGap;

    @Column(name = "taxes", columnDefinition = "jsonb")
    private String taxes; // JSON string of TaxInfo

    @Column(name = "alternatives", columnDefinition = "jsonb")
    private String alternatives; // JSON array of AlternativeProduct

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
