package com.magenta.customers.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Evento base con datos de trazabilidad CloudEvents. */
public interface DomainEvent {
    UUID eventId();
    String eventType();
    Instant occurredAt();
    UUID aggregateId();
    UUID tenantId();
}
