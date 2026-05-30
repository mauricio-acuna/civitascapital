package com.magenta.banks.domain.service;

import com.magenta.banks.domain.model.loansimulation.TaxInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OwnFundsCalculatorTest {

    private OwnFundsCalculatorService service;

    @BeforeEach
    void setUp() { service = new OwnFundsCalculatorService(); }

    @Test
    @DisplayName("Obra nueva 90+5+5: 177.000€ / Catalunya IVA10% AJD1.5%")
    void newBuildNinetyFiveFive() {
        TaxInfo taxes = new TaxInfo(BigDecimal.valueOf(10), BigDecimal.valueOf(1.5), null);
        BigDecimal result = service.calculate(
                BigDecimal.valueOf(177_000), BigDecimal.valueOf(0.90),
                taxes, true, true);
        // Desglose: 10% entrada=17.700 + 5% aplazado=8.850 + 10% IVA=17.700 + 1.5% AJD=2.655 + 1.2% notaría=2.124 = 49.029
        // Rango aceptable [47.000, 51.000]
        assertThat(result.doubleValue()).isBetween(47_000.0, 51_000.0);
    }

    @Test
    @DisplayName("Segunda mano Madrid: ITP 7%, sin tramo aplazado")
    void secondHandMadrid() {
        TaxInfo taxes = new TaxInfo(null, null, BigDecimal.valueOf(7));
        BigDecimal result = service.calculate(
                BigDecimal.valueOf(200_000), BigDecimal.valueOf(0.80),
                taxes, false, false);
        // 20% entrada=40.000 + 7% ITP=14.000 + 1.2% notaría=2.400 = 56.400
        assertThat(result.doubleValue()).isCloseTo(56_400, within(500.0));
    }

    @Test
    @DisplayName("Gap positivo cuando los fondos son insuficientes")
    void positiveGap() {
        BigDecimal required  = BigDecimal.valueOf(40_000);
        BigDecimal available = BigDecimal.valueOf(20_000);
        assertThat(service.fundsGap(required, available)).isEqualByComparingTo(BigDecimal.valueOf(20_000));
    }

    @Test
    @DisplayName("Gap negativo (superávit) cuando hay fondos de sobra")
    void negativeGap() {
        BigDecimal required  = BigDecimal.valueOf(30_000);
        BigDecimal available = BigDecimal.valueOf(50_000);
        assertThat(service.fundsGap(required, available).signum()).isNegative();
    }
}
