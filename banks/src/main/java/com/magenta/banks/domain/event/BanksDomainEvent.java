package com.magenta.banks.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base para todos los eventos de dominio del módulo banks.
 */
public abstract sealed class BanksDomainEvent
    permits ProductPublished, ProductUpdated, ProductDeprecated,
            PreapprovalRequested, PreapprovalApproved, PreapprovalRejected, PreapprovalExpired,
            AppraisalIssued, SimulationCreated {

    private final UUID eventId    = UUID.randomUUID();
    private final Instant occurredOn = Instant.now();
    private final UUID tenantId;

    protected BanksDomainEvent(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID eventId()     { return eventId; }
    public Instant occurredOn() { return occurredOn; }
    public UUID tenantId()    { return tenantId; }

    public abstract String type();
    public abstract String aggregateName();
    public abstract UUID aggregateId();
}
