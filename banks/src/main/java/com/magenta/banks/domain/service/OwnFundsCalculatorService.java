package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.loansimulation.TaxInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula los fondos propios necesarios para el esquema 90+5+5.
 *
 * ownFundsRequired = price × (1 - LTV)       // entrada inicial
 *                  + price × 0.05             // aplazado promotor  (solo 90+5+5)
 *                  + impuestos (IVA+AJD u ITP según tipo operación)
 *                  + price × 0.012            // notaría + registro + gestoría
 */
public class OwnFundsCalculatorService {

    private static final BigDecimal NOTARY_RATE     = BigDecimal.valueOf(0.012);
    private static final BigDecimal DEFERRED_RATE   = BigDecimal.valueOf(0.05);
    private static final int        SCALE           = 2;

    /**
     * @param price          precio del inmueble
     * @param ltvPct         LTV negociado (ej. 0.90 para 90%)
     * @param taxes          tipos impositivos de la CCAA
     * @param isNewBuild     true = obra nueva (IVA+AJD), false = 2ª mano (ITP)
     * @param is90Plus5Plus5 true = incluir tramo aplazado promotor (5%)
     * @return fondos propios necesarios
     */
    public BigDecimal calculate(BigDecimal price, BigDecimal ltvPct, TaxInfo taxes,
                                boolean isNewBuild, boolean is90Plus5Plus5) {
        BigDecimal downpayment = price.multiply(BigDecimal.ONE.subtract(ltvPct));

        BigDecimal deferred = is90Plus5Plus5
                ? price.multiply(DEFERRED_RATE)
                : BigDecimal.ZERO;

        BigDecimal taxAmount;
        if (isNewBuild) {
            BigDecimal iva = taxes.ivaPct() != null
                    ? price.multiply(taxes.ivaPct().divide(BigDecimal.valueOf(100))) : BigDecimal.ZERO;
            BigDecimal ajd = taxes.ajdPct() != null
                    ? price.multiply(taxes.ajdPct().divide(BigDecimal.valueOf(100))) : BigDecimal.ZERO;
            taxAmount = iva.add(ajd);
        } else {
            BigDecimal itp = taxes.itpPct() != null
                    ? price.multiply(taxes.itpPct().divide(BigDecimal.valueOf(100))) : BigDecimal.ZERO;
            taxAmount = itp;
        }

        BigDecimal notary = price.multiply(NOTARY_RATE);

        return downpayment.add(deferred).add(taxAmount).add(notary)
                          .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Diferencia entre fondos requeridos y fondos disponibles.
     * Valor positivo = falta dinero; negativo = hay superávit.
     */
    public BigDecimal fundsGap(BigDecimal required, BigDecimal available) {
        return required.subtract(available);
    }
}
