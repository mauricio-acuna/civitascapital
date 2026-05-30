package com.magenta.areas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "price_indices", schema = "areas")
public class PriceIndexJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "zone_id", nullable = false)
    private UUID zoneId;

    @Column(name = "property_type", nullable = false, length = 24)
    private String propertyType;

    @Column(name = "operation_type", nullable = false, length = 16)
    private String operationType;

    @Column(nullable = false)
    private LocalDate period;

    @Column(name = "price_per_sqm", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerSqm;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "yoy_delta_pct", precision = 6, scale = 3)
    private BigDecimal yoyDeltaPct;

    @Column(name = "mom_delta_pct", precision = 6, scale = 3)
    private BigDecimal momDeltaPct;

    @Column(name = "sample_size", nullable = false)
    private Integer sampleSize;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(nullable = false, length = 24)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // ── Getters & setters ──────────────────────────────────────────────────────

    public UUID getId()              { return id; }
    public void setId(UUID id)       { this.id = id; }
    public UUID getTenantId()        { return tenantId; }
    public void setTenantId(UUID t)  { this.tenantId = t; }
    public UUID getZoneId()          { return zoneId; }
    public void setZoneId(UUID z)    { this.zoneId = z; }
    public String getPropertyType()            { return propertyType; }
    public void setPropertyType(String pt)     { this.propertyType = pt; }
    public String getOperationType()           { return operationType; }
    public void setOperationType(String ot)    { this.operationType = ot; }
    public LocalDate getPeriod()               { return period; }
    public void setPeriod(LocalDate p)         { this.period = p; }
    public BigDecimal getPricePerSqm()         { return pricePerSqm; }
    public void setPricePerSqm(BigDecimal p)   { this.pricePerSqm = p; }
    public String getCurrency()                { return currency; }
    public void setCurrency(String c)          { this.currency = c; }
    public BigDecimal getYoyDeltaPct()         { return yoyDeltaPct; }
    public void setYoyDeltaPct(BigDecimal v)   { this.yoyDeltaPct = v; }
    public BigDecimal getMomDeltaPct()         { return momDeltaPct; }
    public void setMomDeltaPct(BigDecimal v)   { this.momDeltaPct = v; }
    public Integer getSampleSize()             { return sampleSize; }
    public void setSampleSize(Integer s)       { this.sampleSize = s; }
    public BigDecimal getConfidence()          { return confidence; }
    public void setConfidence(BigDecimal c)    { this.confidence = c; }
    public String getSource()                  { return source; }
    public void setSource(String s)            { this.source = s; }
    public Instant getCreatedAt()              { return createdAt; }
    public void setCreatedAt(Instant c)        { this.createdAt = c; }
}
