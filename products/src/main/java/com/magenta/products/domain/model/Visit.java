package com.magenta.products.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Visit(
        UUID id,
        UUID propertyId,
        UUID customerId,
        String agentId,
        Instant slotStart,
        Instant slotEnd,
        VisitMode mode,
        VisitStatus status,
        VisitFeedback feedback,
        Instant createdAt) {

    public record VisitFeedback(int rating, String notes) {}
}
