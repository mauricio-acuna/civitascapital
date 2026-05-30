package com.magenta.products.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Lead(
        UUID id,
        UUID propertyId,
        UUID operationId,
        UUID customerId,
        AnonContact anonContact,
        LeadSource source,
        String message,
        LeadStatus status,
        String assignedAgentId,
        Instant createdAt,
        Instant updatedAt) {

    public record AnonContact(String name, String email, String phone) {}
}
