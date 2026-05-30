package com.magenta.products.domain.event;

import com.magenta.products.domain.model.PropertyType;

import java.time.Instant;
import java.util.UUID;

public record PropertyCreated(
        UUID propertyId,
        UUID tenantId,
        String reference,
        PropertyType type,
        String createdBy,
        Instant occurredAt) {
}
