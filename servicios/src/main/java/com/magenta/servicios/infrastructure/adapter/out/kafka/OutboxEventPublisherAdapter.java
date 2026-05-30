package com.magenta.servicios.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.servicios.domain.port.out.OutboxEventPublisher;
import com.magenta.servicios.infrastructure.adapter.out.persistence.OutboxEventJpaEntity;
import com.magenta.servicios.infrastructure.adapter.out.persistence.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxEventPublisherAdapter implements OutboxEventPublisher {

    private final OutboxEventJpaRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisherAdapter(OutboxEventJpaRepository outboxRepo,
                                       ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, Object event) {
        try {
            OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
            entity.setId(UUID.randomUUID());
            entity.setAggregate(event.getClass().getSimpleName());
            entity.setAggregateId(extractAggregateId(event));
            entity.setType(event.getClass().getName());
            entity.setTopic(topic);
            entity.setPayload(objectMapper.writeValueAsString(event));
            entity.setCreatedAt(Instant.now());
            outboxRepo.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error al persistir evento en outbox: " + e.getMessage(), e);
        }
    }

    private UUID extractAggregateId(Object event) {
        try {
            var method = event.getClass().getMethod("orderId");
            return (UUID) method.invoke(event);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }
}
