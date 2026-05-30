package com.magenta.servicios.domain.port.out;

import java.util.UUID;

public interface AreaClientPort {
    ZoneData getZone(UUID zoneId);
    boolean isAncestorOf(UUID parentZoneId, UUID childZoneId);

    record ZoneData(UUID id, String name, String level, UUID parentId) {}
}
