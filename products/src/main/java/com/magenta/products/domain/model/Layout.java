package com.magenta.products.domain.model;

public record Layout(
        Integer rooms,
        Integer bathrooms,
        Integer terraces,
        Integer parkingSpots,
        Integer storageRooms,
        Integer floor,
        Boolean hasElevator) {
}
