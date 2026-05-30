package com.magenta.customers.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.customers.domain.event.DomainEvent;
import com.magenta.customers.domain.port.out.EventPublisher;
import com.magenta.customers.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaOutboxEventRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementación del puerto EventPublisher.
 * Persiste el evento en la tabla outbox DENTRO de la misma transacción del caso de uso.
 * El polling job lee la tabla y publica en Kafka.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher implements EventPublisher {

    private final JpaOutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void publish(DomainEvent event) {
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .aggregate("Customer")
                .aggregateId(event.aggregateId())
                .tenantId(event.tenantId())
                .type(event.eventType())
                .payload(objectMapper.writeValueAsString(event))
                .build();
        outboxRepo.save(entity);
        log.debug("Outbox event persisted type={} aggregate={}", event.eventType(), event.aggregateId());
    }
}
