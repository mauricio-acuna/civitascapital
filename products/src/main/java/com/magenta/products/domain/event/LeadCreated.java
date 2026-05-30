package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LeadCreated(UUID leadId, UUID propertyId, UUID tenantId, String source, Instant occurredAt) {}
