package com.magenta.products.domain.model;

import java.util.UUID;

public record Location(
        String street,
        String number,
        String floor,
        String door,
        String postalCode,
        GeoPoint coordinates,
        UUID zoneId,
        String zoneName,
        LocationVisibility visibility) {
}
