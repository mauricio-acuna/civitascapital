package com.magenta.banks.domain.model.appraisal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root: Tasación (ECO/805-2003).
 */
public record Appraisal(
    UUID id,
    UUID tenantId,
    UUID propertyId,
    UUID customerId,
    UUID providerId,
    String regulation,        // ECO_805_2003
    BigDecimal marketValue,
    BigDecimal mortgageValue,
    BigDecimal surfaceSqm,
    LocalDate issuedAt,
    LocalDate validUntil,     // issuedAt + 6 meses
    String pdfUrl,
    List<UUID> usedInPreapprovalIds,
    Instant createdAt
) {
    public Appraisal {
        if (propertyId == null)  throw new IllegalArgumentException("propertyId is required");
        if (providerId == null)  throw new IllegalArgumentException("providerId is required");
        if (marketValue == null || marketValue.signum() <= 0)
            throw new IllegalArgumentException("marketValue must be positive");
        if (validUntil == null || !validUntil.isAfter(issuedAt))
            throw new IllegalArgumentException("validUntil must be after issuedAt");
        usedInPreapprovalIds = usedInPreapprovalIds == null ? List.of() : List.copyOf(usedInPreapprovalIds);
    }

    public boolean isValid() {
        return LocalDate.now().isBefore(validUntil);
    }

    /** LTV calculado en base al valor de tasación */
    public BigDecimal computeLtv(BigDecimal loanAmount) {
        return loanAmount.divide(mortgageValue, 4, java.math.RoundingMode.HALF_UP);
    }
}
