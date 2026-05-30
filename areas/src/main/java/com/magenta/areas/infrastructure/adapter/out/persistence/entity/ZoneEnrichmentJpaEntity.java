package com.magenta.areas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "zone_enrichment", schema = "areas")
public class ZoneEnrichmentJpaEntity {

    @Id
    @Column(name = "zone_id", columnDefinition = "uuid")
    private UUID zoneId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "fiber_coverage_pct")
    private Integer fiberCoveragePct;

    @Column(name = "has_hospital", nullable = false)
    private boolean hasHospital;

    @Column(name = "hospital_kind", nullable = false, length = 32)
    private String hospitalKind;

    @Column(name = "train_to_hub_minutes")
    private Integer trainToHubMinutes;

    @Column(name = "highway_distance_km", precision = 8, scale = 2)
    private BigDecimal highwayDistanceKm;

    @Column(name = "supermarkets_count")
    private Integer supermarketsCount;

    @Column(name = "risk_occupation_score")
    private Integer riskOccupationScore;

    @Column(name = "depopulation_risk", nullable = false, length = 8)
    private String depopulationRisk;

    @Column(name = "quality_of_life_index")
    private Integer qualityOfLifeIndex;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Getters & setters ──────────────────────────────────────────────────────

    public UUID getZoneId()                      { return zoneId; }
    public void setZoneId(UUID z)                { this.zoneId = z; }
    public UUID getTenantId()                    { return tenantId; }
    public void setTenantId(UUID t)              { this.tenantId = t; }
    public Integer getFiberCoveragePct()         { return fiberCoveragePct; }
    public void setFiberCoveragePct(Integer v)   { this.fiberCoveragePct = v; }
    public boolean isHasHospital()               { return hasHospital; }
    public void setHasHospital(boolean v)        { this.hasHospital = v; }
    public String getHospitalKind()              { return hospitalKind; }
    public void setHospitalKind(String v)        { this.hospitalKind = v; }
    public Integer getTrainToHubMinutes()        { return trainToHubMinutes; }
    public void setTrainToHubMinutes(Integer v)  { this.trainToHubMinutes = v; }
    public BigDecimal getHighwayDistanceKm()     { return highwayDistanceKm; }
    public void setHighwayDistanceKm(BigDecimal v){ this.highwayDistanceKm = v; }
    public Integer getSupermarketsCount()        { return supermarketsCount; }
    public void setSupermarketsCount(Integer v)  { this.supermarketsCount = v; }
    public Integer getRiskOccupationScore()      { return riskOccupationScore; }
    public void setRiskOccupationScore(Integer v){ this.riskOccupationScore = v; }
    public String getDepopulationRisk()          { return depopulationRisk; }
    public void setDepopulationRisk(String v)    { this.depopulationRisk = v; }
    public Integer getQualityOfLifeIndex()       { return qualityOfLifeIndex; }
    public void setQualityOfLifeIndex(Integer v) { this.qualityOfLifeIndex = v; }
    public Instant getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(Instant v)          { this.updatedAt = v; }
}
