package com.magenta.banks.infrastructure.adapter.out.persistence.loanproduct;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_products", schema = "banks")
@Getter @Setter
public class LoanProductJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "bank_id", nullable = false)
    private UUID bankId;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "rate_type", nullable = false, length = 20)
    private String rateType;

    @Column(name = "tin_initial_pct", nullable = false, precision = 6, scale = 4)
    private BigDecimal tinInitialPct;

    @Column(name = "tin_index_reference", length = 20)
    private String tinIndexReference;

    @Column(name = "tin_margin_pct", precision = 6, scale = 4)
    private BigDecimal tinMarginPct;

    @Column(name = "tin_fixed_years")
    private Integer tinFixedYears;

    @Column(name = "ltv_max_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal ltvMaxPct;

    @Column(name = "ltc_max_pct", precision = 5, scale = 2)
    private BigDecimal ltcMaxPct;

    @Column(name = "ticket_min", nullable = false, precision = 14, scale = 2)
    private BigDecimal ticketMin;

    @Column(name = "ticket_max", nullable = false, precision = 14, scale = 2)
    private BigDecimal ticketMax;

    @Column(name = "term_min_months", nullable = false)
    private int termMinMonths;

    @Column(name = "term_max_months", nullable = false)
    private int termMaxMonths;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String eligibility;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String bundling;

    @Column(name = "fee_opening_pct", precision = 5, scale = 3)
    private BigDecimal feeOpeningPct;

    @Column(name = "fee_study_pct", precision = 5, scale = 3)
    private BigDecimal feeStudyPct;

    @Column(name = "fee_early_repayment_pct", precision = 5, scale = 3)
    private BigDecimal feeEarlyRepaymentPct;

    @Column(nullable = false, length = 24)
    private String scheme;

    @Column(name = "promo_code", length = 40)
    private String promoCode;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;
}
