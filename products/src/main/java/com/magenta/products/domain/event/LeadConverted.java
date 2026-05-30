package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LeadConverted(UUID leadId, UUID propertyId, UUID transactionId, Instant occurredAt) {}
