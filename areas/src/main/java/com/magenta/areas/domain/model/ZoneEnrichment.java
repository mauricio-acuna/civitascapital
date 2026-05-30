package com.magenta.areas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Datos de enriquecimiento de servicios y riesgo por zona.
 */
public class ZoneEnrichment {

    private final UUID zoneId;
    private final UUID tenantId;
    private Integer fiberCoveragePct;
    private boolean hasHospital;
    private HospitalKind hospitalKind;
    private Integer trainToHubMinutes;
    private BigDecimal highwayDistanceKm;
    private Integer supermarketsCount;
    private Integer riskOccupationScore;
    private DepopulationRisk depopulationRisk;
    private Integer qualityOfLifeIndex;
    private Instant updatedAt;

    private ZoneEnrichment(UUID zoneId, UUID tenantId) {
        this.zoneId = zoneId;
        this.tenantId = tenantId;
        this.hasHospital = false;
        this.hospitalKind = HospitalKind.NONE;
        this.depopulationRisk = DepopulationRisk.LOW;
        this.updatedAt = Instant.now();
    }

    public static ZoneEnrichment empty(UUID zoneId, UUID tenantId) {
        return new ZoneEnrichment(zoneId, tenantId);
    }

    public static ZoneEnrichment reconstitute(UUID zoneId, UUID tenantId,
                                               Integer fiberCoveragePct, boolean hasHospital,
                                               HospitalKind hospitalKind, Integer trainToHubMinutes,
                                               BigDecimal highwayDistanceKm, Integer supermarketsCount,
                                               Integer riskOccupationScore, DepopulationRisk depopulationRisk,
                                               Integer qualityOfLifeIndex, Instant updatedAt) {
        ZoneEnrichment e = new ZoneEnrichment(zoneId, tenantId);
        e.fiberCoveragePct   = fiberCoveragePct;
        e.hasHospital        = hasHospital;
        e.hospitalKind       = hospitalKind;
        e.trainToHubMinutes  = trainToHubMinutes;
        e.highwayDistanceKm  = highwayDistanceKm;
        e.supermarketsCount  = supermarketsCount;
        e.riskOccupationScore = riskOccupationScore;
        e.depopulationRisk   = depopulationRisk;
        e.qualityOfLifeIndex = qualityOfLifeIndex;
        e.updatedAt          = updatedAt;
        return e;
    }

    public void update(Integer fiberCoveragePct, boolean hasHospital, HospitalKind hospitalKind,
                       Integer trainToHubMinutes, BigDecimal highwayDistanceKm,
                       Integer supermarketsCount, Integer riskOccupationScore,
                       DepopulationRisk depopulationRisk, Integer qualityOfLifeIndex) {
        this.fiberCoveragePct   = fiberCoveragePct;
        this.hasHospital        = hasHospital;
        this.hospitalKind       = hospitalKind != null ? hospitalKind : HospitalKind.NONE;
        this.trainToHubMinutes  = trainToHubMinutes;
        this.highwayDistanceKm  = highwayDistanceKm;
        this.supermarketsCount  = supermarketsCount;
        this.riskOccupationScore = riskOccupationScore;
        this.depopulationRisk   = depopulationRisk != null ? depopulationRisk : DepopulationRisk.LOW;
        this.qualityOfLifeIndex = qualityOfLifeIndex;
        this.updatedAt = Instant.now();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getZoneId()                { return zoneId; }
    public UUID getTenantId()              { return tenantId; }
    public Integer getFiberCoveragePct()   { return fiberCoveragePct; }
    public boolean isHasHospital()         { return hasHospital; }
    public HospitalKind getHospitalKind()  { return hospitalKind; }
    public Integer getTrainToHubMinutes()  { return trainToHubMinutes; }
    public BigDecimal getHighwayDistanceKm() { return highwayDistanceKm; }
    public Integer getSupermarketsCount()  { return supermarketsCount; }
    public Integer getRiskOccupationScore(){ return riskOccupationScore; }
    public DepopulationRisk getDepopulationRisk() { return depopulationRisk; }
    public Integer getQualityOfLifeIndex() { return qualityOfLifeIndex; }
    public Instant getUpdatedAt()          { return updatedAt; }
}
