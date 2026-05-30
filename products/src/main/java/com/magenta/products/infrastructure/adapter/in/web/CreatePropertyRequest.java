package com.magenta.products.infrastructure.adapter.in.web;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record CreatePropertyRequest(
        @NotBlank String reference,
        @NotBlank String type,
        String subtype,
        @NotBlank String ownerType,
        String ownerId,
        String ownerName,
        @NotBlank String street,
        String streetNumber,
        String floor,
        String door,
        @NotBlank @Size(min = 5, max = 10) String postalCode,
        @DecimalMin("-90") @DecimalMax("90") double lat,
        @DecimalMin("-180") @DecimalMax("180") double lng,
        @NotNull @DecimalMin("1") BigDecimal builtSqm,
        BigDecimal usefulSqm,
        BigDecimal plotSqm,
        Integer rooms,
        Integer bathrooms,
        Integer terraces,
        Integer parkingSpots,
        Integer storageRooms,
        Integer propertyFloor,
        Boolean hasElevator,
        String condition,
        Integer buildYear,
        Set<String> features,
        Set<String> tags) {
}
