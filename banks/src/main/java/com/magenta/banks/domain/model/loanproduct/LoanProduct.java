package com.magenta.banks.domain.model.loanproduct;

import com.magenta.banks.domain.model.LoanCategory;
import com.magenta.banks.domain.model.ProductStatus;
import com.magenta.banks.domain.model.Scheme;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root: Producto hipotecario de una entidad financiera.
 */
public record LoanProduct(
    UUID id,
    UUID tenantId,
    UUID bankId,
    String sku,
    String name,
    LoanCategory category,
    RateInfo rateInfo,
    BigDecimal ltvMaxPct,
    BigDecimal ltcMaxPct,
    BigDecimal ticketMin,
    BigDecimal ticketMax,
    int termMinMonths,
    int termMaxMonths,
    EligibilityRules eligibility,
    List<String> bundling,           // códigos de cross-sell (seguro hogar, vida, nómina…)
    BigDecimal feeOpeningPct,
    BigDecimal feeStudyPct,
    BigDecimal feeEarlyRepaymentPct,
    Scheme scheme,
    String promoCode,
    LocalDate validFrom,
    LocalDate validTo,
    ProductStatus status,
    Instant createdAt,
    Instant updatedAt,
    long version
) {
    public LoanProduct {
        if (bankId == null)   throw new IllegalArgumentException("bankId is required");
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku is required");
        if (ltvMaxPct == null || ltvMaxPct.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("ltvMaxPct must be <= 100");
        if (scheme == Scheme.NINETY_FIVE_FIVE && ltvMaxPct.compareTo(BigDecimal.valueOf(90)) < 0)
            throw new IllegalArgumentException("NINETY_FIVE_FIVE scheme requires ltvMaxPct >= 90");
        bundling  = bundling  == null ? List.of() : List.copyOf(bundling);
        eligibility = eligibility == null ? EligibilityRules.empty() : eligibility;
    }

    public LoanProduct publish() {
        return withStatus(ProductStatus.ACTIVE);
    }

    public LoanProduct deprecate() {
        return withStatus(ProductStatus.DEPRECATED);
    }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    private LoanProduct withStatus(ProductStatus newStatus) {
        return new LoanProduct(id, tenantId, bankId, sku, name, category, rateInfo,
                ltvMaxPct, ltcMaxPct, ticketMin, ticketMax, termMinMonths, termMaxMonths,
                eligibility, bundling, feeOpeningPct, feeStudyPct, feeEarlyRepaymentPct,
                scheme, promoCode, validFrom, validTo, newStatus, createdAt, Instant.now(), version + 1);
    }
}
