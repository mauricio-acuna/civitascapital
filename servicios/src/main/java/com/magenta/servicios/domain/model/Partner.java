package com.magenta.servicios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class Partner {

    private final UUID id;
    private final UUID tenantId;
    private final String code;
    private final String name;
    private final PartnerKind kind;
    private Set<ServiceCode> services;
    private Set<UUID> coverageZoneIds;
    private BigDecimal commissionPct;
    private BigDecimal rating;
    private Short npsScore;
    private boolean active;
    private String contractRef;
    private final Instant createdAt;

    public Partner(UUID id, UUID tenantId, String code, String name, PartnerKind kind,
                   Set<ServiceCode> services, Set<UUID> coverageZoneIds,
                   BigDecimal commissionPct, BigDecimal rating, Short npsScore,
                   boolean active, String contractRef, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.kind = kind;
        this.services = services != null ? Set.copyOf(services) : Set.of();
        this.coverageZoneIds = coverageZoneIds != null ? Set.copyOf(coverageZoneIds) : Set.of();
        this.commissionPct = commissionPct;
        this.rating = rating;
        this.npsScore = npsScore;
        this.active = active;
        this.contractRef = contractRef;
        this.createdAt = createdAt;
    }

    public boolean covers(ServiceCode serviceCode, UUID zoneId) {
        return active && services.contains(serviceCode) && coverageZoneIds.contains(zoneId);
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public PartnerKind getKind() { return kind; }
    public Set<ServiceCode> getServices() { return services; }
    public Set<UUID> getCoverageZoneIds() { return coverageZoneIds; }
    public BigDecimal getCommissionPct() { return commissionPct; }
    public BigDecimal getRating() { return rating; }
    public Short getNpsScore() { return npsScore; }
    public boolean isActive() { return active; }
    public String getContractRef() { return contractRef; }
    public Instant getCreatedAt() { return createdAt; }
}
