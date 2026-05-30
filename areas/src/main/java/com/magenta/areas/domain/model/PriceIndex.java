package com.magenta.areas.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Índice de precio €/m² por zona, tipología y período.
 * Inmutable salvo recálculo.
 */
public class PriceIndex {

    private final UUID id;
    private final UUID tenantId;
    private final UUID zoneId;
    private final PropertyType propertyType;
    private final OperationType operationType;
    private final LocalDate period;           // primer día del mes
    private final Money pricePerSqm;
    private final BigDecimal yoyDeltaPct;
    private final BigDecimal momDeltaPct;
    private final int sampleSize;
    private final BigDecimal confidence;      // 0..1
    private final SourceRef source;

    private PriceIndex(UUID id, UUID tenantId, UUID zoneId, PropertyType propertyType,
                       OperationType operationType, LocalDate period, Money pricePerSqm,
                       BigDecimal yoyDeltaPct, BigDecimal momDeltaPct,
                       int sampleSize, BigDecimal confidence, SourceRef source) {
        this.id = id;
        this.tenantId = tenantId;
        this.zoneId = zoneId;
        this.propertyType = propertyType;
        this.operationType = operationType;
        this.period = period;
        this.pricePerSqm = pricePerSqm;
        this.yoyDeltaPct = yoyDeltaPct;
        this.momDeltaPct = momDeltaPct;
        this.sampleSize = sampleSize;
        this.confidence = confidence;
        this.source = source;
    }

    /**
     * Calcula el índice a partir de una lista de transacciones (§9.1 del spec).
     *
     * @param samples  precios €/m² de cada transacción (ya winsurizados externamente)
     */
    public static PriceIndex compute(UUID tenantId, UUID zoneId, PropertyType propertyType,
                                     OperationType operationType, LocalDate period,
                                     java.util.List<BigDecimal> samples,
                                     BigDecimal yoyDeltaPct, BigDecimal momDeltaPct) {

        if (samples == null || samples.isEmpty()) {
            throw new IllegalArgumentException("Cannot compute PriceIndex without samples");
        }

        BigDecimal median = calculateMedian(samples);
        int n = samples.size();
        // confidence = clamp(n / 30, 0, 1)
        BigDecimal conf = BigDecimal.valueOf(n).divide(BigDecimal.valueOf(30), 3, RoundingMode.HALF_UP);
        if (conf.compareTo(BigDecimal.ONE) > 0) conf = BigDecimal.ONE;

        return new PriceIndex(UUID.randomUUID(), tenantId, zoneId, propertyType,
                operationType, period, Money.euros(median),
                yoyDeltaPct, momDeltaPct, n, conf, SourceRef.INTERNAL);
    }

    public static PriceIndex reconstitute(UUID id, UUID tenantId, UUID zoneId,
                                          PropertyType propertyType, OperationType operationType,
                                          LocalDate period, Money pricePerSqm,
                                          BigDecimal yoyDeltaPct, BigDecimal momDeltaPct,
                                          int sampleSize, BigDecimal confidence, SourceRef source) {
        return new PriceIndex(id, tenantId, zoneId, propertyType, operationType, period,
                pricePerSqm, yoyDeltaPct, momDeltaPct, sampleSize, confidence, source);
    }

    /** true si la confianza es suficiente para publicar al exterior (≥ 0.5). */
    public boolean isPublishable() {
        return confidence.compareTo(new BigDecimal("0.5")) >= 0;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static BigDecimal calculateMedian(java.util.List<BigDecimal> values) {
        java.util.List<BigDecimal> sorted = values.stream().sorted().toList();
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        } else {
            BigDecimal lower = sorted.get(size / 2 - 1);
            BigDecimal upper = sorted.get(size / 2);
            return lower.add(upper).divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId()                   { return id; }
    public UUID getTenantId()             { return tenantId; }
    public UUID getZoneId()               { return zoneId; }
    public PropertyType getPropertyType() { return propertyType; }
    public OperationType getOperationType(){ return operationType; }
    public LocalDate getPeriod()          { return period; }
    public Money getPricePerSqm()         { return pricePerSqm; }
    public BigDecimal getYoyDeltaPct()    { return yoyDeltaPct; }
    public BigDecimal getMomDeltaPct()    { return momDeltaPct; }
    public int getSampleSize()            { return sampleSize; }
    public BigDecimal getConfidence()     { return confidence; }
    public SourceRef getSource()          { return source; }
}
