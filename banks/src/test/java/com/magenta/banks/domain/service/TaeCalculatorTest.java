package com.magenta.banks.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TaeCalculatorTest {

    private TaeCalculatorService service;

    @BeforeEach
    void setUp() { service = new TaeCalculatorService(); }

    /**
     * Caso golden: hipoteca de 100.000 € al 3% fijo, 20 años, sin comisiones ni seguros.
     * La TAE debe coincidir con el TIN (3%) cuando no hay costes adicionales.
     */
    @Test
    @DisplayName("Sin comisiones ni seguros: TAE ≈ TIN")
    void taeEqualsWhenNoCosts() {
        BigDecimal tae = service.calculateMortgageTae(
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(3.0),
                240,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
        assertThat(tae.doubleValue()).isCloseTo(3.0, within(0.05));
    }

    /**
     * Con comisión de apertura del 1 %: la TAE debe ser > TIN.
     */
    @Test
    @DisplayName("Con comisión 1%: TAE > TIN")
    void taeHigherWithFee() {
        BigDecimal tae = service.calculateMortgageTae(
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(3.0),
                240,
                BigDecimal.valueOf(1.0),  // 1% comisión
                BigDecimal.ZERO);
        assertThat(tae.doubleValue()).isGreaterThan(3.0);
    }

    /**
     * Con seguro mensual: la TAE debe ser > TIN.
     */
    @Test
    @DisplayName("Con seguro mensual 50€: TAE > TIN")
    void taeHigherWithInsurance() {
        BigDecimal tae = service.calculateMortgageTae(
                BigDecimal.valueOf(150_000),
                BigDecimal.valueOf(3.5),
                360,
                BigDecimal.ZERO,
                BigDecimal.valueOf(50));   // 50 €/mes seguro
        assertThat(tae.doubleValue()).isGreaterThan(3.5);
    }

    /**
     * Ejemplo del simulador del spec: ~4.12 % con 159.300 € al 3,5%/360m.
     * TAE sin costes debe estar próxima al TIN.
     */
    @Test
    @DisplayName("Ejemplo spec: 159.300€ / 3.5% / 360m — TAE razonable")
    void specExample() {
        BigDecimal tae = service.calculateMortgageTae(
                BigDecimal.valueOf(159_300),
                BigDecimal.valueOf(3.5),
                360,
                BigDecimal.valueOf(0.5),  // 0.5% apertura
                BigDecimal.valueOf(30));   // 30 €/mes seguro
        // La TAE debe estar entre 3.5% y 5%
        assertThat(tae.doubleValue()).isBetween(3.5, 5.0);
    }
}
