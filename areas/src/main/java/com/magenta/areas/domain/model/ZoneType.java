package com.magenta.areas.domain.model;

public enum ZoneType {
    COUNTRY,
    REGION,
    PROVINCE,
    COUNTY,
    MUNICIPALITY,
    DISTRICT,
    NEIGHBORHOOD,
    URBANIZATION,
    STREET,
    BUILDING;

    /** Devuelve el tipo padre inmediato en la jerarquía. Null para COUNTRY. */
    public ZoneType parentType() {
        return switch (this) {
            case COUNTRY       -> null;
            case REGION        -> COUNTRY;
            case PROVINCE      -> REGION;
            case COUNTY        -> PROVINCE;
            case MUNICIPALITY  -> COUNTY;
            case DISTRICT      -> MUNICIPALITY;
            case NEIGHBORHOOD  -> DISTRICT;
            case URBANIZATION  -> NEIGHBORHOOD;
            case STREET        -> URBANIZATION;
            case BUILDING      -> STREET;
        };
    }
}
