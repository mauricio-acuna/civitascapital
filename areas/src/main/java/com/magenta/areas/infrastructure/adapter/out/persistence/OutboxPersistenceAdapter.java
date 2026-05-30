package com.magenta.areas.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.domain.event.DomainEvent;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxPersistenceAdapter implements OutboxPort {

    private final OutboxJpaRepository jpaRepo;
    private final ObjectMapper objectMapper;

    public OutboxPersistenceAdapter(OutboxJpaRepository jpaRepo, ObjectMapper objectMapper) {
        this.jpaRepo      = jpaRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(event.getEventId());
        entity.setAggregate(resolveAggregate(event.getType()));
        entity.setAggregateId(resolveAggregateId(event));
        entity.setType(event.getType());
        entity.setPayload(serialize(event));
        entity.setCreatedAt(Instant.now());
        jpaRepo.save(entity);
    }

    private String resolveAggregate(String eventType) {
        if (eventType.contains("Zone") && !eventType.contains("Enrichment")) return "Zone";
        if (eventType.contains("PriceIndex")) return "PriceIndex";
        if (eventType.contains("Enrichment")) return "ZoneEnrichment";
        return "Unknown";
    }

    private UUID resolveAggregateId(DomainEvent event) {
        return switch (event) {
            case com.magenta.areas.domain.event.ZoneCreated e    -> e.getZoneId();
            case com.magenta.areas.domain.event.ZoneUpdated e    -> e.getZoneId();
            case com.magenta.areas.domain.event.ZoneDeprecated e -> e.getZoneId();
            case com.magenta.areas.domain.event.PriceIndexPublished e -> e.getPriceIndexId();
            case com.magenta.areas.domain.event.ZoneEnrichmentUpdated e -> e.getZoneId();
        };
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize domain event", e);
        }
    }
}
