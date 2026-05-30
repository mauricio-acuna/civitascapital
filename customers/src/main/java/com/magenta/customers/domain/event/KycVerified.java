package com.magenta.customers.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class KycVerified implements DomainEvent {
    UUID eventId;
    Instant occurredAt;
    UUID aggregateId;
    UUID tenantId;
    Integer kycScore;
    Instant expiresAt;

    @Override public String eventType() { return "com.magenta.customers.KycVerified"; }
}
