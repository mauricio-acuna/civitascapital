package com.magenta.products.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.util.Set;

public record UpdatePropertyRequest(
        String subtype,
        String condition,
        Integer buildYear,
        Integer lastRenovationYear,
        Integer rooms,
        Integer bathrooms,
        Integer terraces,
        Integer parkingSpots,
        Integer storageRooms,
        Integer propertyFloor,
        Boolean hasElevator,
        Set<String> features,
        Set<String> tags) {
}
