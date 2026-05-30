package com.magenta.areas.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base para todos los eventos de dominio del módulo areas.
 * Sin dependencias de Spring.
 */
public abstract sealed class DomainEvent
        permits ZoneCreated, ZoneUpdated, ZoneDeprecated,
                PriceIndexPublished, ZoneEnrichmentUpdated {

    private final UUID eventId;
    private final Instant occurredAt;
    private final String actorId;

    protected DomainEvent(String actorId) {
        this.eventId    = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.actorId    = actorId;
    }

    public UUID getEventId()       { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getActorId()     { return actorId; }

    public abstract String getType();
}
