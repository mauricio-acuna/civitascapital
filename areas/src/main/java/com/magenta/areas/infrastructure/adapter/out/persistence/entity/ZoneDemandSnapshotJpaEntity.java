package com.magenta.areas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "zone_demand_snapshots", schema = "areas")
public class ZoneDemandSnapshotJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "zone_id", nullable = false)
    private UUID zoneId;

    @Column(nullable = false)
    private LocalDate period;

    @Column(nullable = false)
    private Integer searches;

    @Column(nullable = false)
    private Integer leads;

    @Column(name = "viewed_properties", nullable = false)
    private Integer viewedProperties;

    @Column(name = "saved_searches", nullable = false)
    private Integer savedSearches;

    @Column(name = "supply_demand_ratio", precision = 8, scale = 4)
    private BigDecimal supplyDemandRatio;

    // ── Getters & setters ──────────────────────────────────────────────────────

    public UUID getId()                       { return id; }
    public void setId(UUID id)                { this.id = id; }
    public UUID getZoneId()                   { return zoneId; }
    public void setZoneId(UUID z)             { this.zoneId = z; }
    public LocalDate getPeriod()              { return period; }
    public void setPeriod(LocalDate p)        { this.period = p; }
    public Integer getSearches()              { return searches; }
    public void setSearches(Integer s)        { this.searches = s; }
    public Integer getLeads()                 { return leads; }
    public void setLeads(Integer l)           { this.leads = l; }
    public Integer getViewedProperties()      { return viewedProperties; }
    public void setViewedProperties(Integer v){ this.viewedProperties = v; }
    public Integer getSavedSearches()         { return savedSearches; }
    public void setSavedSearches(Integer s)   { this.savedSearches = s; }
    public BigDecimal getSupplyDemandRatio()  { return supplyDemandRatio; }
    public void setSupplyDemandRatio(BigDecimal v){ this.supplyDemandRatio = v; }
}
