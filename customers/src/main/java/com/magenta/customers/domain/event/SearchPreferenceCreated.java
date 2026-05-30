package com.magenta.customers.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class SearchPreferenceCreated implements DomainEvent {
    UUID eventId;
    Instant occurredAt;
    UUID aggregateId;   // customerId
    UUID tenantId;
    UUID preferenceId;

    @Override public String eventType() { return "com.magenta.customers.SearchPreferenceCreated"; }
}
