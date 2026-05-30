package com.magenta.products.domain.model;

public record OwnerInfo(
        String ownerType,   // PRIVATE, BANK, FUND, DEVELOPER
        String ownerId,
        String ownerName) {
}
