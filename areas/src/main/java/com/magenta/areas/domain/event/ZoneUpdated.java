package com.magenta.areas.domain.event;

import com.magenta.areas.domain.model.GeoPoint;
import com.magenta.areas.domain.model.ZoneType;

import java.util.UUID;

public final class ZoneUpdated extends DomainEvent {

    private final UUID zoneId;
    private final String code;
    private final String name;
    private final ZoneType type;
    private final UUID parentId;
    private final GeoPoint centroid;
    private final UUID tenantId;

    public ZoneUpdated(UUID zoneId, String code, String name, ZoneType type,
                       UUID parentId, GeoPoint centroid, UUID tenantId, String actorId) {
        super(actorId);
        this.zoneId   = zoneId;
        this.code     = code;
        this.name     = name;
        this.type     = type;
        this.parentId = parentId;
        this.centroid = centroid;
        this.tenantId = tenantId;
    }

    @Override public String getType() { return "com.magenta.areas.ZoneUpdated"; }

    public UUID getZoneId()       { return zoneId; }
    public String getCode()       { return code; }
    public String getName()       { return name; }
    public ZoneType getZoneType() { return type; }
    public UUID getParentId()     { return parentId; }
    public GeoPoint getCentroid() { return centroid; }
    public UUID getTenantId()     { return tenantId; }
}
