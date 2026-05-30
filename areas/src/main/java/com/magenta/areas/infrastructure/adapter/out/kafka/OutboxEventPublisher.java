package com.magenta.areas.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.infrastructure.adapter.out.persistence.OutboxJpaRepository;
import com.magenta.areas.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Polling publisher: lee eventos pendientes del Outbox y los publica en Kafka.
 * Garantía at-least-once; los consumidores deben ser idempotentes.
 */
@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxJpaRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxJpaRepository outboxRepo,
                                 KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.outboxRepo    = outboxRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper  = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)   // cada 2 s
    @Transactional
    public void publishPending() {
        List<OutboxEventJpaEntity> pending = outboxRepo.findUnpublished();
        for (OutboxEventJpaEntity event : pending) {
            try {
                String topic = resolveTopic(event.getType());
                kafkaTemplate.send(topic, event.getAggregateId().toString(), buildMessage(event));
                outboxRepo.markPublished(event.getId(), Instant.now());
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), ex.getMessage(), ex);
            }
        }
    }

    private String resolveTopic(String eventType) {
        if (eventType.contains("ZoneCreated") || eventType.contains("ZoneUpdated")
                || eventType.contains("ZoneDeprecated")) {
            return "magenta.areas.zone.v1";
        }
        if (eventType.contains("PriceIndex")) {
            return "magenta.areas.price-index.v1";
        }
        if (eventType.contains("Enrichment")) {
            return "magenta.areas.enrichment.v1";
        }
        return "magenta.areas.events.v1";
    }

    private String buildMessage(OutboxEventJpaEntity event) {
        try {
            Map<String, Object> envelope = Map.of(
                    "specversion", "1.0",
                    "id", event.getId().toString(),
                    "source", "/magenta/areas",
                    "type", event.getType(),
                    "time", Instant.now().toString(),
                    "subject", event.getAggregate() + ":" + event.getAggregateId(),
                    "datacontenttype", "application/json",
                    "tenantid", event.getTenantId() != null ? event.getTenantId().toString() : "",
                    "data", objectMapper.readValue(event.getPayload(), Object.class));
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot build Kafka message for event " + event.getId(), e);
        }
    }
}
