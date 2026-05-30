package com.magenta.areas.domain.port.in;

import com.magenta.areas.domain.model.GeoPoint;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneType;

import java.util.Set;
import java.util.UUID;

public interface CreateZonePort {

    record Command(UUID tenantId, String code, String name, ZoneType type,
                   UUID parentId, GeoPoint centroid, String actorId) {}

    Zone execute(Command command);
}
