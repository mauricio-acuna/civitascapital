package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LeadLost(UUID leadId, UUID propertyId, String reason, Instant occurredAt) {}
