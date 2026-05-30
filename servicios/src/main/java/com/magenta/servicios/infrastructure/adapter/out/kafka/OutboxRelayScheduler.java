package com.magenta.servicios.infrastructure.adapter.out.kafka;

import com.magenta.servicios.infrastructure.adapter.out.persistence.OutboxEventJpaEntity;
import com.magenta.servicios.infrastructure.adapter.out.persistence.OutboxEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polling outbox relay: cada 5 segundos publica eventos pendientes a Kafka.
 * Garantía: at-least-once. Los consumidores son idempotentes vía processed_events.
 */
@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxEventJpaRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelayScheduler(OutboxEventJpaRepository outboxRepo,
                                 KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepo = outboxRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${magenta.outbox.relay.delay-ms:5000}")
    @Transactional
    public void relay() {
        List<OutboxEventJpaEntity> pending = outboxRepo.findUnpublished();

        for (OutboxEventJpaEntity event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Error publicando evento {} a {}: {}", event.getId(), event.getTopic(), ex.getMessage());
                            } else {
                                outboxRepo.markPublished(event.getId());
                            }
                        });
            } catch (Exception e) {
                log.error("Error enviando evento outbox {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
