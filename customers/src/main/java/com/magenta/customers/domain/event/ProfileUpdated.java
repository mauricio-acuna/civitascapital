package com.magenta.customers.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class ProfileUpdated implements DomainEvent {
    UUID eventId;
    Instant occurredAt;
    UUID aggregateId;
    UUID tenantId;
    String updatedBy;

    @Override public String eventType() { return "com.magenta.customers.ProfileUpdated"; }
}
