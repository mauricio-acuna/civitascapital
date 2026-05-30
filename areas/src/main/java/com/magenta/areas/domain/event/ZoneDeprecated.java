package com.magenta.areas.domain.event;

import com.magenta.areas.domain.model.ZoneType;

import java.util.UUID;

public final class ZoneDeprecated extends DomainEvent {

    private final UUID zoneId;
    private final String code;
    private final String name;
    private final ZoneType type;
    private final UUID tenantId;

    public ZoneDeprecated(UUID zoneId, String code, String name,
                          ZoneType type, UUID tenantId, String actorId) {
        super(actorId);
        this.zoneId   = zoneId;
        this.code     = code;
        this.name     = name;
        this.type     = type;
        this.tenantId = tenantId;
    }

    @Override public String getType() { return "com.magenta.areas.ZoneDeprecated"; }

    public UUID getZoneId()       { return zoneId; }
    public String getCode()       { return code; }
    public String getName()       { return name; }
    public ZoneType getZoneType() { return type; }
    public UUID getTenantId()     { return tenantId; }
}
