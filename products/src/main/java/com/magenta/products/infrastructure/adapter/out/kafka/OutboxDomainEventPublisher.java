package com.magenta.products.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.products.domain.port.out.DomainEventPublisher;
import com.magenta.products.infrastructure.adapter.out.persistence.OutboxEventJpaEntity;
import com.magenta.products.infrastructure.adapter.out.persistence.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Writes domain events to the outbox table (same transaction as the aggregate save).
 * A separate scheduler reads and publishes to Kafka.
 */
@Component
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxDomainEventPublisher(OutboxEventJpaRepository outboxRepository,
                                       ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(Object domainEvent) {
        String type = domainEvent.getClass().getSimpleName();
        String aggregate = resolveAggregate(type);
        UUID aggregateId = resolveAggregateId(domainEvent);

        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setAggregate(aggregate);
        entity.setAggregateId(aggregateId);
        entity.setType("com.magenta.products." + type);
        entity.setPayload(toJson(domainEvent));
        entity.setCreatedAt(Instant.now());
        outboxRepository.save(entity);
    }

    private String resolveAggregate(String eventType) {
        if (eventType.startsWith("Property")) return "Property";
        if (eventType.startsWith("Operation")) return "Operation";
        if (eventType.startsWith("Transaction")) return "Transaction";
        if (eventType.startsWith("Lead")) return "Lead";
        if (eventType.startsWith("Visit")) return "Visit";
        return "Unknown";
    }

    private UUID resolveAggregateId(Object event) {
        try {
            // All domain events have a first UUID field as aggregate ID
            var method = event.getClass().getMethods()[0];
            return (UUID) event.getClass().getMethod(
                    event.getClass().getRecordComponents()[0].getName()).invoke(event);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { throw new IllegalStateException("Cannot serialize event", e); }
    }
}
