package com.magenta.areas.domain.event;

import com.magenta.areas.domain.model.DepopulationRisk;
import com.magenta.areas.domain.model.HospitalKind;

import java.util.UUID;

public final class ZoneEnrichmentUpdated extends DomainEvent {

    private final UUID zoneId;
    private final UUID tenantId;
    private final Integer fiberCoveragePct;
    private final HospitalKind hospitalKind;
    private final DepopulationRisk depopulationRisk;

    public ZoneEnrichmentUpdated(UUID zoneId, UUID tenantId, Integer fiberCoveragePct,
                                  HospitalKind hospitalKind, DepopulationRisk depopulationRisk,
                                  String actorId) {
        super(actorId);
        this.zoneId           = zoneId;
        this.tenantId         = tenantId;
        this.fiberCoveragePct = fiberCoveragePct;
        this.hospitalKind     = hospitalKind;
        this.depopulationRisk = depopulationRisk;
    }

    @Override public String getType() { return "com.magenta.areas.ZoneEnrichmentUpdated"; }

    public UUID getZoneId()                      { return zoneId; }
    public UUID getTenantId()                    { return tenantId; }
    public Integer getFiberCoveragePct()         { return fiberCoveragePct; }
    public HospitalKind getHospitalKind()        { return hospitalKind; }
    public DepopulationRisk getDepopulationRisk(){ return depopulationRisk; }
}
