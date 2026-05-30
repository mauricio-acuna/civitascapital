package com.magenta.banks.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FrenchAmortizationTest {

    private FrenchAmortizationService service;

    @BeforeEach
    void setUp() { service = new FrenchAmortizationService(); }

    @Test
    @DisplayName("Cuota conocida: 159.300 € al 3,5% / 360 meses ≈ 715,42 €")
    void knownMortgagePayment() {
        BigDecimal result = service.monthlyPayment(
                BigDecimal.valueOf(159_300), BigDecimal.valueOf(3.5), 360);
        // margen de ±0.50 € por redondeos
        assertThat(result.doubleValue()).isCloseTo(715.42, within(0.50));
    }

    @Test
    @DisplayName("Cuota con TIN=0: principal / plazo")
    void zeroInterest() {
        BigDecimal result = service.monthlyPayment(
                BigDecimal.valueOf(120_000), BigDecimal.ZERO, 120);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    @DisplayName("Total de intereses positivo en préstamo con interés")
    void totalInterestPositive() {
        BigDecimal interest = service.totalInterest(
                BigDecimal.valueOf(100_000), BigDecimal.valueOf(4.0), 360);
        assertThat(interest.doubleValue()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Coste total = cuota × plazo")
    void totalCostEqualsMonthlyTimesMonths() {
        BigDecimal monthly = service.monthlyPayment(
                BigDecimal.valueOf(200_000), BigDecimal.valueOf(2.8), 300);
        BigDecimal total   = service.totalCost(
                BigDecimal.valueOf(200_000), BigDecimal.valueOf(2.8), 300);
        assertThat(total).isEqualByComparingTo(monthly.multiply(BigDecimal.valueOf(300)));
    }
}
