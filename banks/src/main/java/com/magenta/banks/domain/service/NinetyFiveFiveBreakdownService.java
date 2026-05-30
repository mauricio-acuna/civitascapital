package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.loansimulation.TaxInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Desglose financiero del esquema 90+5+5:
 * 90% financiacion bancaria, 5% tramo aplazado/promotor y 5% entrada del comprador.
 *
 * La salida separa fondos necesarios en firma de compromiso total para explicar
 * la operacion con claridad comercial y prudencia regulatoria.
 */
public class NinetyFiveFiveBreakdownService {

    private static final BigDecimal BANK_LTV = BigDecimal.valueOf(0.90);
    private static final BigDecimal DEFERRED_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal BUYER_DOWN_PAYMENT_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal CLOSING_COST_RATE = BigDecimal.valueOf(0.012);
    private static final int SCALE = 2;

    public Breakdown calculate(BigDecimal propertyPrice, TaxInfo taxes, boolean newBuild) {
        requirePositive(propertyPrice, "propertyPrice");
        if (taxes == null) {
            taxes = new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null);
        }

        BigDecimal bankLoan = money(propertyPrice.multiply(BANK_LTV));
        BigDecimal developerDeferred = money(propertyPrice.multiply(DEFERRED_RATE));
        BigDecimal buyerDownPayment = money(propertyPrice.multiply(BUYER_DOWN_PAYMENT_RATE));
        BigDecimal taxesAmount = money(calculateTaxes(propertyPrice, taxes, newBuild));
        BigDecimal closingCosts = money(propertyPrice.multiply(CLOSING_COST_RATE));
        BigDecimal requiredAtSigning = buyerDownPayment.add(taxesAmount).add(closingCosts);
        BigDecimal totalCommittedOwnFunds = requiredAtSigning.add(developerDeferred);

        return new Breakdown(
                bankLoan,
                developerDeferred,
                buyerDownPayment,
                taxesAmount,
                closingCosts,
                requiredAtSigning,
                totalCommittedOwnFunds,
                BANK_LTV,
                DEFERRED_RATE,
                BUYER_DOWN_PAYMENT_RATE);
    }

    private BigDecimal calculateTaxes(BigDecimal propertyPrice, TaxInfo taxes, boolean newBuild) {
        if (newBuild) {
            BigDecimal iva = percentage(propertyPrice, taxes.ivaPct());
            BigDecimal ajd = percentage(propertyPrice, taxes.ajdPct());
            return iva.add(ajd);
        }
        return percentage(propertyPrice, taxes.itpPct());
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal pct) {
        if (pct == null) return BigDecimal.ZERO;
        return amount.multiply(pct).divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }

    public record Breakdown(
            BigDecimal bankLoan,
            BigDecimal developerDeferred,
            BigDecimal buyerDownPayment,
            BigDecimal taxes,
            BigDecimal closingCosts,
            BigDecimal requiredOwnFundsAtSigning,
            BigDecimal totalCommittedOwnFunds,
            BigDecimal bankLtv,
            BigDecimal deferredRate,
            BigDecimal buyerDownPaymentRate
    ) {}
}

