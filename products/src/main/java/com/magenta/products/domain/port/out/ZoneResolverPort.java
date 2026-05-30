package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.GeoPoint;

import java.util.UUID;

public interface ZoneResolverPort {
    /**
     * Resolves a zone ID from coordinates, calling the areas module.
     * Returns null if no zone found.
     */
    UUID resolveZone(GeoPoint coordinates);

    /**
     * Validates that the given coordinates fall within the zone boundary.
     */
    boolean isWithinZoneBoundary(GeoPoint coordinates, UUID zoneId);
}
