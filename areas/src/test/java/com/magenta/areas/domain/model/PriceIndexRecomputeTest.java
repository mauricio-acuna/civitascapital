package com.magenta.areas.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PriceIndexRecomputeTest {

    private static final UUID TENANT  = UUID.randomUUID();
    private static final UUID ZONE_ID = UUID.randomUUID();
    private static final LocalDate PERIOD = LocalDate.of(2026, 5, 1);

    @Test
    @DisplayName("Compute calcula la mediana correctamente (impar)")
    void compute_medianOdd() {
        List<BigDecimal> samples = List.of(
                new BigDecimal("1000"),
                new BigDecimal("1200"),
                new BigDecimal("900"),
                new BigDecimal("1100"),
                new BigDecimal("950"));

        PriceIndex idx = PriceIndex.compute(TENANT, ZONE_ID, PropertyType.FLAT,
                OperationType.SALE, PERIOD, samples, null, null);

        assertThat(idx.getPricePerSqm().amount()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("Compute calcula la mediana correctamente (par)")
    void compute_medianEven() {
        List<BigDecimal> samples = List.of(
                new BigDecimal("1000"),
                new BigDecimal("1200"));

        PriceIndex idx = PriceIndex.compute(TENANT, ZONE_ID, PropertyType.FLAT,
                OperationType.SALE, PERIOD, samples, null, null);

        assertThat(idx.getPricePerSqm().amount()).isEqualByComparingTo("1100");
    }

    @Test
    @DisplayName("confidence = 1 cuando hay 30 o más muestras")
    void compute_confidenceMax() {
        List<BigDecimal> samples = java.util.stream.IntStream.range(0, 30)
                .mapToObj(i -> BigDecimal.valueOf(1000 + i))
                .toList();

        PriceIndex idx = PriceIndex.compute(TENANT, ZONE_ID, PropertyType.FLAT,
                OperationType.SALE, PERIOD, samples, null, null);

        assertThat(idx.getConfidence()).isEqualByComparingTo("1.000");
        assertThat(idx.isPublishable()).isTrue();
    }

    @Test
    @DisplayName("confidence < 0.5 con menos de 15 muestras → no publicable")
    void compute_notPublishable() {
        List<BigDecimal> samples = List.of(
                new BigDecimal("1000"),
                new BigDecimal("1100"));

        PriceIndex idx = PriceIndex.compute(TENANT, ZONE_ID, PropertyType.FLAT,
                OperationType.SALE, PERIOD, samples, null, null);

        assertThat(idx.isPublishable()).isFalse();
    }

    @Test
    @DisplayName("Lanza excepción si samples está vacío")
    void compute_emptySamples() {
        assertThatThrownBy(() ->
                PriceIndex.compute(TENANT, ZONE_ID, PropertyType.FLAT,
                        OperationType.SALE, PERIOD, List.of(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("samples");
    }
}
