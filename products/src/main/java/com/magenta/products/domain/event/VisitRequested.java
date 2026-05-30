package com.magenta.products.domain.event;

import java.time.Instant;
import java.util.UUID;

public record VisitRequested(UUID visitId, UUID propertyId, UUID customerId,
                              String agentId, Instant slotStart, Instant occurredAt) {}

public record VisitRequested(UUID visitId, UUID propertyId, UUID customerId,
                              String agentId, Instant slotStart, Instant occurredAt) {}
