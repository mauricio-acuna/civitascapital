package com.magenta.areas.domain.model;

/**
 * Value Object inmutable que representa un punto geográfico.
 * SRID 4326 (WGS 84).
 */
public record GeoPoint(double lat, double lng) {

    public GeoPoint {
        if (lat < -90 || lat > 90)   throw new IllegalArgumentException("lat must be in [-90, 90]: " + lat);
        if (lng < -180 || lng > 180) throw new IllegalArgumentException("lng must be in [-180, 180]: " + lng);
    }
}
