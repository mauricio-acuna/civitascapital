package com.magenta.customers.domain.event;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class FinancialSnapshotPublished implements DomainEvent {
    UUID eventId;
    Instant occurredAt;
    UUID aggregateId;   // customerId
    UUID tenantId;
    UUID snapshotId;
    LocalDate asOf;
    /** Banda de ingresos ofuscada (no PII): LOW/MEDIUM/HIGH */
    String incomeBand;
    BigDecimal confidence;

    @Override public String eventType() { return "com.magenta.customers.FinancialSnapshotPublished"; }
}
