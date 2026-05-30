package com.magenta.banks.infrastructure.adapter.in.web.dto;

import com.magenta.banks.domain.model.loanproduct.LoanProduct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LoanProductResponse(
    UUID id,
    UUID bankId,
    String sku,
    String name,
    String category,
    String rateType,
    BigDecimal tinInitialPct,
    BigDecimal ltvMaxPct,
    BigDecimal ticketMin,
    BigDecimal ticketMax,
    int termMinMonths,
    int termMaxMonths,
    String scheme,
    String promoCode,
    LocalDate validFrom,
    LocalDate validTo,
    String status
) {
    public static LoanProductResponse from(LoanProduct p) {
        return new LoanProductResponse(
                p.id(), p.bankId(), p.sku(), p.name(),
                p.category().name(), p.rateInfo().rateType().name(),
                p.rateInfo().initialPct(), p.ltvMaxPct(),
                p.ticketMin(), p.ticketMax(), p.termMinMonths(), p.termMaxMonths(),
                p.scheme().name(), p.promoCode(), p.validFrom(), p.validTo(),
                p.status().name());
    }
}
