package com.magenta.banks.domain.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calcula la cuota mensual por el sistema de amortización francés (cuota constante).
 *
 * c = P · i / (1 - (1+i)^-n)
 *
 * Para tipo variable se expone un método con escenario estresado (+100 bps).
 */
public class FrenchAmortizationService {

    private static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);
    private static final int SCALE = 2;

    /**
     * @param principal   importe del préstamo
     * @param annualTinPct tipo de interés anual nominal (ej. 3.5 para 3,5%)
     * @param termMonths  plazo en meses
     * @return cuota mensual redondeada a 2 decimales
     */
    public BigDecimal monthlyPayment(BigDecimal principal, BigDecimal annualTinPct, int termMonths) {
        if (annualTinPct.compareTo(BigDecimal.ZERO) == 0) {
            // Préstamo sin interés
            return principal.divide(BigDecimal.valueOf(termMonths), SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal i = annualTinPct.divide(BigDecimal.valueOf(1200), MC);  // TIN mensual
        // (1+i)^-n
        BigDecimal onePlusI  = BigDecimal.ONE.add(i, MC);
        BigDecimal discount  = onePlusI.pow(-termMonths, MC);
        BigDecimal denominator = BigDecimal.ONE.subtract(discount, MC);

        return principal.multiply(i, MC)
                        .divide(denominator, MC)
                        .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Coste total del crédito (suma de cuotas).
     */
    public BigDecimal totalCost(BigDecimal principal, BigDecimal annualTinPct, int termMonths) {
        return monthlyPayment(principal, annualTinPct, termMonths)
               .multiply(BigDecimal.valueOf(termMonths));
    }

    /**
     * Total de intereses pagados.
     */
    public BigDecimal totalInterest(BigDecimal principal, BigDecimal annualTinPct, int termMonths) {
        return totalCost(principal, annualTinPct, termMonths).subtract(principal);
    }
}
