package com.magenta.products.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Favorite(UUID customerId, UUID propertyId, Instant createdAt) {
}
