package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PropertyArchived(UUID propertyId, UUID tenantId, Instant occurredAt) {}
