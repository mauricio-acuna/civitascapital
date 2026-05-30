package com.magenta.customers.application;

import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.in.AffordabilityPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * UC-C10: Calcular capacidad hipotecaria estimada.
 *
 * maxPaymentBdE      = max(0, netIncome × 0.40 − otherDebt)  [límite duro BdE]
 * maxPaymentInternal = max(0, netIncome × 0.30 − otherDebt)  [política Magenta]
 * targetTicket       = PV(rate/12, term, -maxPaymentInternal) / LTV
 */
@Service
public class AffordabilityService implements AffordabilityPort {

    @Value("${magenta.affordability.bde-ratio:0.40}")
    private double bdeRatio;

    @Value("${magenta.affordability.internal-ratio:0.30}")
    private double internalRatio;

    @Value("${magenta.affordability.default-term-months:300}")
    private int defaultTermMonths;

    @Value("${magenta.affordability.default-tin:0.035}")
    private double defaultTin;

    @Value("${magenta.affordability.default-ltv:0.80}")
    private double defaultLtv;

    @Override
    public ComputedAffordability compute(FinancialSnapshot snapshot) {
        BigDecimal net = snapshot.getNetIncomeMonthly();
        BigDecimal otherDebt = snapshot.getOtherDebtMonthly() != null
                ? snapshot.getOtherDebtMonthly() : BigDecimal.ZERO;

        BigDecimal maxBdE = net.multiply(BigDecimal.valueOf(bdeRatio))
                .subtract(otherDebt).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal maxInternal = net.multiply(BigDecimal.valueOf(internalRatio))
                .subtract(otherDebt).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal capital = presentValue(maxInternal, defaultTermMonths, defaultTin);
        BigDecimal targetTicket = capital.divide(BigDecimal.valueOf(defaultLtv), 2, RoundingMode.HALF_UP);

        BigDecimal savingsRunway = snapshot.getOwnFunds() != null && maxInternal.compareTo(BigDecimal.ZERO) > 0
                ? snapshot.getOwnFunds().divide(maxInternal, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ComputedAffordability.builder()
                .maxAffordablePaymentBdE(maxBdE)
                .maxAffordablePaymentInternal(maxInternal)
                .targetTicketPrice(targetTicket)
                .savingsRunwayMonths(savingsRunway)
                .build();
    }

    /**
     * Calcula el valor presente de una anualidad (capital máximo para una cuota dada).
     * PV = pmt × [1 − (1 + r)^−n] / r
     */
    private BigDecimal presentValue(BigDecimal monthlyPayment, int termMonths, double annualTin) {
        double r = annualTin / 12.0;
        double pmt = monthlyPayment.doubleValue();
        if (r == 0) return monthlyPayment.multiply(BigDecimal.valueOf(termMonths));
        double pv = pmt * (1 - Math.pow(1 + r, -termMonths)) / r;
        return BigDecimal.valueOf(pv).setScale(2, RoundingMode.HALF_UP);
    }
}
