package com.magenta.servicios.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PropertySearchBrief {

    private final UUID orderId;
    private BigDecimal targetTicket;
    private List<UUID> zoneIds;
    private List<String> propertyTypes;
    private String mustHaves;      // JSONB array
    private String niceToHaves;    // JSONB array
    private LocalDate deadline;
    private List<UUID> shortlist;

    public PropertySearchBrief(UUID orderId, BigDecimal targetTicket, List<UUID> zoneIds,
                                List<String> propertyTypes, String mustHaves, String niceToHaves,
                                LocalDate deadline, List<UUID> shortlist) {
        this.orderId = orderId;
        this.targetTicket = targetTicket;
        this.zoneIds = zoneIds != null ? List.copyOf(zoneIds) : List.of();
        this.propertyTypes = propertyTypes != null ? List.copyOf(propertyTypes) : List.of();
        this.mustHaves = mustHaves;
        this.niceToHaves = niceToHaves;
        this.deadline = deadline;
        this.shortlist = shortlist != null ? List.copyOf(shortlist) : List.of();
    }

    public void addProperty(UUID propertyId) {
        List<UUID> updated = new java.util.ArrayList<>(shortlist);
        updated.add(propertyId);
        this.shortlist = List.copyOf(updated);
    }

    public UUID getOrderId() { return orderId; }
    public BigDecimal getTargetTicket() { return targetTicket; }
    public List<UUID> getZoneIds() { return zoneIds; }
    public List<String> getPropertyTypes() { return propertyTypes; }
    public String getMustHaves() { return mustHaves; }
    public String getNiceToHaves() { return niceToHaves; }
    public LocalDate getDeadline() { return deadline; }
    public List<UUID> getShortlist() { return shortlist; }
}
