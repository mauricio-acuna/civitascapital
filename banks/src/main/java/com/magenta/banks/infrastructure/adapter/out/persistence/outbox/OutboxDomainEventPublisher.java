package com.magenta.banks.infrastructure.adapter.out.persistence.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.banks.domain.event.BanksDomainEvent;
import com.magenta.banks.domain.port.out.DomainEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Persiste los eventos de dominio en la tabla outbox_event dentro de la misma transacción.
 * Un poller separado (OutboxPoller) los publica en Kafka.
 */
@Component
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxDomainEventPublisher(OutboxEventJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper     = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(BanksDomainEvent event) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(event.eventId());
        entity.setAggregate(event.aggregateName());
        entity.setAggregateId(event.aggregateId());
        entity.setType(event.type());
        entity.setCreatedAt(Instant.now());

        try {
            entity.setPayload(objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            entity.setPayload("{}");
        }

        outboxRepository.save(entity);
    }
}
