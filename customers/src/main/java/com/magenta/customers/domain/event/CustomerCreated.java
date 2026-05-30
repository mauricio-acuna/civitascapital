package com.magenta.customers.domain.event;

import com.magenta.customers.domain.model.CustomerType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class CustomerCreated implements DomainEvent {
    UUID eventId;
    Instant occurredAt;
    UUID aggregateId;
    UUID tenantId;
    CustomerType customerType;
    String displayName;
    String createdBy;

    @Override public String eventType() { return "com.magenta.customers.CustomerCreated"; }
}
