package com.magenta.products.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PropertyView(
        UUID id,
        UUID propertyId,
        UUID userId,
        UUID anonId,
        Instant at,
        String channel,
        String referrer) {
}
