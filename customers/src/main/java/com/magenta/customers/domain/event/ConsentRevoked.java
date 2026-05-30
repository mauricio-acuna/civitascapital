package com.magenta.customers.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class ConsentRevoked implements DomainEvent {
    UUID eventId;
    Instant occurredAt;
    UUID aggregateId;
    UUID tenantId;
    String purpose;

    @Override public String eventType() { return "com.magenta.customers.ConsentRevoked"; }
}
