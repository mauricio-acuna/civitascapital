package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PropertyUpdated(UUID propertyId, UUID tenantId, Instant occurredAt) {}
