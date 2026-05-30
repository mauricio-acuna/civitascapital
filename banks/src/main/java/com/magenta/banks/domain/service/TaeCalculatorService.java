package com.magenta.banks.domain.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calcula la TAE conforme a la Directiva 2014/17/UE (Anexo I),
 * implementada en España por RD 309/2019 y Circular BdE 5/2012.
 *
 * Resuelve iterativamente (Newton-Raphson) la ecuación de equivalencia financiera:
 *
 *   Σ Ck · (1 + TAE)^(-tk) = 0
 *
 * donde los flujos positivos son disposiciones y los negativos son pagos.
 */
public class TaeCalculatorService {

    private static final MathContext MC          = new MathContext(20, RoundingMode.HALF_UP);
    private static final int         MAX_ITER    = 200;
    private static final double      TOLERANCE   = 1e-10;
    private static final int         RESULT_SCALE = 4;   // 4 decimales -> ej. 4.1234 %

    /**
     * Flujo de caja: signo positivo = dinero recibido, negativo = pagado.
     *
     * @param amount   importe (puede ser positivo o negativo)
     * @param tYears   tiempo en años desde la disposición inicial
     */
    public record CashFlow(double amount, double tYears) {}

    /**
     * Calcula la TAE a partir de una lista de flujos de caja.
     *
     * @param cashFlows lista de flujos (disposición(es) + cuotas + comisiones)
     * @return TAE en porcentaje (ej. 4.12 para 4,12%)
     * @throws ArithmeticException si no converge
     */
    public BigDecimal calculate(List<CashFlow> cashFlows) {
        double x = 0.04; // semilla inicial: 4 %
        for (int iter = 0; iter < MAX_ITER; iter++) {
            double fx  = npv(cashFlows, x);
            double dfx = npvDerivative(cashFlows, x);
            if (Math.abs(dfx) < 1e-15) break;
            double xNew = x - fx / dfx;
            if (Math.abs(xNew - x) < TOLERANCE) {
                x = xNew;
                break;
            }
            x = xNew;
            if (iter == MAX_ITER - 1) {
                throw new ArithmeticException("TAE calculation did not converge");
            }
        }
        return BigDecimal.valueOf(x * 100).setScale(RESULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Construye los flujos de caja estándar de una hipoteca y calcula la TAE.
     *
     * @param principal      importe del préstamo
     * @param annualTinPct   TIN anual (%)
     * @param termMonths     plazo en meses
     * @param feeOpeningPct  comisión de apertura (%)
     * @param monthlyInsurance cuota mensual de seguros vinculados (€)
     * @return TAE en % (ej. 4.12)
     */
    public BigDecimal calculateMortgageTae(BigDecimal principal, BigDecimal annualTinPct,
                                            int termMonths, BigDecimal feeOpeningPct,
                                            BigDecimal monthlyInsurance) {
        FrenchAmortizationService french = new FrenchAmortizationService();
        double p          = principal.doubleValue();
        double fee        = feeOpeningPct.doubleValue() / 100.0 * p;
        double insurance  = monthlyInsurance == null ? 0.0 : monthlyInsurance.doubleValue();
        double monthlyPay = french.monthlyPayment(principal, annualTinPct, termMonths).doubleValue();

        // Flujo inicial: disposición neta (descontando la comisión de apertura que se paga al inicio)
        var flows = new java.util.ArrayList<CashFlow>();
        flows.add(new CashFlow(p - fee, 0.0));

        for (int m = 1; m <= termMonths; m++) {
            double t = m / 12.0;
            flows.add(new CashFlow(-(monthlyPay + insurance), t));
        }
        return calculate(flows);
    }

    // ── internos ───────────────────────────────────────────────────────────────

    private double npv(List<CashFlow> flows, double rate) {
        double sum = 0.0;
        for (CashFlow cf : flows) {
            sum += cf.amount() * Math.pow(1.0 + rate, -cf.tYears());
        }
        return sum;
    }

    private double npvDerivative(List<CashFlow> flows, double rate) {
        double sum = 0.0;
        for (CashFlow cf : flows) {
            if (cf.tYears() == 0.0) continue;
            sum += cf.amount() * (-cf.tYears()) * Math.pow(1.0 + rate, -cf.tYears() - 1.0);
        }
        return sum;
    }
}
