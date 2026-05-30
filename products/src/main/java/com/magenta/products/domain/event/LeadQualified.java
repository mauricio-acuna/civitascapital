package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LeadQualified(UUID leadId, UUID propertyId, String qualifiedBy, Instant occurredAt) {}
