package com.magenta.products.infrastructure.adapter.out.kafka;

import com.magenta.products.infrastructure.adapter.out.persistence.OutboxEventJpaEntity;
import com.magenta.products.infrastructure.adapter.out.persistence.OutboxEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polls the outbox table and publishes pending events to Kafka.
 * Marks each event as published after successful send.
 */
@Component
public class OutboxPollingPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPollingPublisher.class);

    private static final String TOPIC_PROPERTY  = "magenta.products.property.v1";
    private static final String TOPIC_OPERATION = "magenta.products.operation.v1";
    private static final String TOPIC_LEAD      = "magenta.products.lead.v1";
    private static final String TOPIC_VISIT     = "magenta.products.visit.v1";
    private static final String TOPIC_TX        = "magenta.products.transaction.v1";

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${magenta.outbox.batch-size:50}")
    private int batchSize;

    public OutboxPollingPublisher(OutboxEventJpaRepository outboxRepository,
                                   KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${magenta.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEventJpaEntity> pending = outboxRepository.findUnpublished();
        if (pending.isEmpty()) return;

        pending.stream().limit(batchSize).forEach(event -> {
            String topic = resolveTopic(event.getType());
            kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            outboxRepository.markPublished(event.getId());
                            log.debug("Published event {} to topic {}", event.getId(), topic);
                        } else {
                            log.error("Failed to publish event {}: {}", event.getId(), ex.getMessage());
                        }
                    });
        });
    }

    private String resolveTopic(String eventType) {
        if (eventType.contains("Operation")) return TOPIC_OPERATION;
        if (eventType.contains("Transaction")) return TOPIC_TX;
        if (eventType.contains("Lead")) return TOPIC_LEAD;
        if (eventType.contains("Visit")) return TOPIC_VISIT;
        return TOPIC_PROPERTY;
    }
}
