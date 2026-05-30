package com.magenta.areas.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Snapshot mensual de métricas de demanda para una zona.
 */
public class ZoneDemandSnapshot {

    private final UUID id;
    private final UUID zoneId;
    private final LocalDate period;
    private int searches;
    private int leads;
    private int viewedProperties;
    private int savedSearches;
    private BigDecimal supplyDemandRatio;

    private ZoneDemandSnapshot(UUID id, UUID zoneId, LocalDate period) {
        this.id = id;
        this.zoneId = zoneId;
        this.period = period;
    }

    public static ZoneDemandSnapshot reconstitute(UUID id, UUID zoneId, LocalDate period,
                                                   int searches, int leads, int viewedProperties,
                                                   int savedSearches, BigDecimal supplyDemandRatio) {
        ZoneDemandSnapshot s = new ZoneDemandSnapshot(id, zoneId, period);
        s.searches = searches;
        s.leads = leads;
        s.viewedProperties = viewedProperties;
        s.savedSearches = savedSearches;
        s.supplyDemandRatio = supplyDemandRatio;
        return s;
    }

    public void incrementSearches(int delta) { this.searches += delta; }
    public void incrementLeads(int delta)    { this.leads += delta; }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId()                       { return id; }
    public UUID getZoneId()                   { return zoneId; }
    public LocalDate getPeriod()              { return period; }
    public int getSearches()                  { return searches; }
    public int getLeads()                     { return leads; }
    public int getViewedProperties()          { return viewedProperties; }
    public int getSavedSearches()             { return savedSearches; }
    public BigDecimal getSupplyDemandRatio()  { return supplyDemandRatio; }
}
